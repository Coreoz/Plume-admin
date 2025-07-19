package com.coreoz.plume.admin.db.daos;

import com.coreoz.plume.admin.db.generated.LogApi;
import com.coreoz.plume.admin.db.generated.QLogApi;
import com.coreoz.plume.admin.db.generated.QLogHeader;
import com.coreoz.plume.admin.services.logapi.LogApiFilters;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;
import com.google.common.base.Strings;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class LogApiDao extends CrudDaoQuerydsl<LogApi> {

    private static final NumberPath<Long> ID_SQL_PATH = Expressions.numberPath(Long.class, "id");

    @Inject
    public LogApiDao(
        TransactionManagerQuerydsl transactionManager
    ) {
        super(transactionManager, QLogApi.logApi);
    }

    private SQLQuery<Tuple> buildFilterLogsQuery(
		String method,
		Integer statusCode,
		String apiName,
		String url,
		Instant startDate,
		Instant endDate
	) {
		BooleanExpression filters = applyFilters(method, statusCode, apiName, url, startDate, endDate);
    	return transactionManager
			.selectQuery()
			.select(
				QLogApi.logApi.id,
				QLogApi.logApi.date,
				QLogApi.logApi.method,
				QLogApi.logApi.apiName,
				QLogApi.logApi.url,
				QLogApi.logApi.statusCode
			)
			.from(QLogApi.logApi)
			.where(filters);
	}

    public List<LogApiTrimmed> fetchTrimmedLogs(
		Integer limit,
		String method,
		Integer statusCode,
		String apiName,
		String url,
		Instant startDate,
		Instant endDate
	) {
        return buildFilterLogsQuery(method, statusCode, apiName, url, startDate, endDate)
			.limit(limit)
            .orderBy(QLogApi.logApi.date.desc())
            .fetch()
            .stream()
            .map(tuple -> new LogApiTrimmed(
					tuple.get(QLogApi.logApi.id),
					tuple.get(QLogApi.logApi.date),
					tuple.get(QLogApi.logApi.method),
					tuple.get(QLogApi.logApi.apiName),
					tuple.get(QLogApi.logApi.url),
					tuple.get(QLogApi.logApi.statusCode)
				)
			).collect(Collectors.toList());
    }

    private BooleanExpression applyFilters(
		String method,
		Integer statusCode,
		String apiName,
		String url,
		Instant startDate,
		Instant endDate
	) {
		BooleanExpression filters = Expressions.asBoolean(true).isTrue();

		if (!Strings.isNullOrEmpty(method)) {
			filters = filters.and(QLogApi.logApi.method.eq(method));
		}
		if (statusCode != null) {
			filters = filters.and(
				QLogApi.logApi.statusCode.eq(statusCode)
			);
		}
		if (!Strings.isNullOrEmpty(apiName)) {
			filters = filters.and(
				QLogApi.logApi.apiName.containsIgnoreCase(apiName)
			);
		}
		if (!Strings.isNullOrEmpty(url)) {
			filters = filters.and(
				QLogApi.logApi.url.containsIgnoreCase(url)
			);
		}

		if (startDate != null) {
			filters = filters.and(
				QLogApi.logApi.date.goe(startDate)
			);
		}
		if (endDate != null) {
			filters = filters.and(
				QLogApi.logApi.date.loe(endDate)
			);
		}

        return filters;
    }

	public Optional<Tuple> findByIdWithMaxLength(Long id, int maxLength) {
		return Optional.ofNullable(
			this.transactionManager.selectQuery()
				.select(
					QLogApi.logApi.id,
					QLogApi.logApi.statusCode,
					QLogApi.logApi.apiName,
					QLogApi.logApi.date,
					QLogApi.logApi.method,
					QLogApi.logApi.url,
					QLogApi.logApi.bodyRequest.substring(0, maxLength).as(QLogApi.logApi.bodyRequest),
					QLogApi.logApi.bodyResponse.substring(0, maxLength).as(QLogApi.logApi.bodyResponse),
					QLogApi.logApi.bodyRequest.length(),
					QLogApi.logApi.bodyRequest.length()
				)
				.from(QLogApi.logApi)
				.where(QLogApi.logApi.id.eq(id))
				.fetchOne()
		);
	}

    public LogApiFilters fetchAvailableFilters() {
        return this.transactionManager.executeAndReturn(connection ->
            LogApiFilters.of(
                this.listApiNames(connection),
                this.listStatusCodes(connection)
            )
        );
    }

    public List<String> listApiNames() {
        return this.transactionManager.executeAndReturn(this::listApiNames);
    }

    private List<String> listApiNames(Connection connection) {
        return transactionManager
            .selectQuery(connection)
            .select(QLogApi.logApi.apiName)
            .distinct()
            .from(QLogApi.logApi)
            .fetch();
    }

    private List<Integer> listStatusCodes(Connection connection) {
        return this.transactionManager.selectQuery(connection)
            .select(QLogApi.logApi.statusCode)
            .distinct()
            .from(QLogApi.logApi)
            .fetch();
    }

    public long deleteLogsOverLimit(String apiName, int maxLogByApi)  {
		SimpleExpression<Long> logApiIdsToDeleteQuery = SQLExpressions
			.select(QLogApi.logApi.id)
			.from(QLogApi.logApi)
			.where(QLogApi.logApi.apiName.eq(apiName))
			.orderBy(QLogApi.logApi.date.desc())
			.limit(Long.MAX_VALUE)
			.offset(maxLogByApi)
			.as("alias_for_mysql");
		// this is needed because MySQL does not allow limit in sub-query :/
		SQLQuery<Long> logApiIdsToDeleteQueryWrapperForMysql = SQLExpressions
			.select(ID_SQL_PATH)
			.from(logApiIdsToDeleteQuery);

		return transactionManager.executeAndReturn(connection -> {
			transactionManager
				.delete(QLogHeader.logHeader, connection)
				.where(QLogHeader.logHeader.idLogApi.in(
					logApiIdsToDeleteQueryWrapperForMysql
				))
				.execute();

			return transactionManager
				.delete(QLogApi.logApi, connection)
				.where(QLogApi.logApi.id.in(logApiIdsToDeleteQueryWrapperForMysql))
				.execute();
		});
    }

	public long deleteOldLogs(Instant minimumLogDate) {
		return transactionManager.executeAndReturn(connection -> {
			transactionManager
				.delete(QLogHeader.logHeader, connection)
				.where(QLogHeader.logHeader.idLogApi.in(
					SQLExpressions
						.select(QLogApi.logApi.id)
						.from(QLogApi.logApi)
						.where(QLogApi.logApi.date.before(minimumLogDate))
				))
				.execute();

			return transactionManager
				.delete(QLogApi.logApi, connection)
				.where(QLogApi.logApi.date.before(minimumLogDate))
				.execute();
		});
	}

}

