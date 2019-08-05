package com.coreoz.plume.admin.db.daos;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.generated.LogApi;
import com.coreoz.plume.admin.db.generated.QLogApi;
import com.coreoz.plume.admin.db.generated.QLogHeader;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;

@Singleton
public class LogApiDao extends CrudDaoQuerydsl<LogApi> {

    @Inject
    public LogApiDao(TransactionManagerQuerydsl transactionManager) {
        super(transactionManager, QLogApi.logApi);
    }

    public List<LogApiTrimmed> fetchTrimmedLogs() {
        return transactionManager
            .selectQuery()
            .select(
            	QLogApi.logApi.id,
            	QLogApi.logApi.date,
            	QLogApi.logApi.method,
            	QLogApi.logApi.api,
            	QLogApi.logApi.url,
            	QLogApi.logApi.statusCode
            )
            .from(QLogApi.logApi)
            .orderBy(QLogApi.logApi.date.desc())
            .fetch()
            .stream()
            .map(tuple -> new LogApiTrimmed(
            	tuple.get(QLogApi.logApi.id),
            	tuple.get(QLogApi.logApi.date),
            	tuple.get(QLogApi.logApi.method),
            	tuple.get(QLogApi.logApi.api),
            	tuple.get(QLogApi.logApi.url),
            	tuple.get(QLogApi.logApi.statusCode)
            ))
            .collect(Collectors.toList());
    }

    public List<String> listApiNames() {
        return transactionManager
            .selectQuery()
            .select(QLogApi.logApi.api)
            .distinct()
            .from(QLogApi.logApi)
            .fetch();
    }

    public long deleteLogsOverLimit(String apiName, int maxLogByApi)  {
		SQLQuery<Long> logApiIdsToDeleteQuery = SQLExpressions
			.select(QLogApi.logApi.id)
			.from(QLogApi.logApi)
			.where(QLogApi.logApi.api.eq(apiName))
			.orderBy(QLogApi.logApi.date.desc())
			.limit(Long.MAX_VALUE)
			.offset(maxLogByApi);

		return transactionManager.executeAndReturn(connection -> {
			transactionManager
				.delete(QLogHeader.logHeader, connection)
				.where(QLogHeader.logHeader.idLogApi.in(
					logApiIdsToDeleteQuery
				))
				.execute();

			return transactionManager
				.delete(QLogApi.logApi, connection)
				.where(QLogApi.logApi.id.in(logApiIdsToDeleteQuery))
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

