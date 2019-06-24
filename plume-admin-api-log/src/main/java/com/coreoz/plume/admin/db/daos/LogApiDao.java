package com.coreoz.plume.admin.db.daos;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.generated.LogApi;
import com.coreoz.plume.admin.db.generated.QLogApi;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

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
            	tuple.get(QLogApi.logApi.method),
            	tuple.get(QLogApi.logApi.api),
            	tuple.get(QLogApi.logApi.url),
            	tuple.get(QLogApi.logApi.statusCode)
            ))
            .collect(Collectors.toList());
    }

    public List<LogApi> getLogsbyApiName(String apiName) {
        return transactionManager
            .selectQuery()
            .select(QLogApi.logApi)
            .from(QLogApi.logApi)
            .where(QLogApi.logApi.api.eq(apiName))
            .fetch();
    }

    public List<String> getListApiNames() {
        return transactionManager
            .selectQuery()
            .select(QLogApi.logApi.api)
            .distinct()
            .from(QLogApi.logApi)
            .fetch();
    }

    public List<LogApi> getListApibyDate(int numberOfDays) {
        return transactionManager
            .selectQuery()
            .select(QLogApi.logApi)
            .from(QLogApi.logApi)
            .where(QLogApi.logApi.date.before(Instant.now().minus(Duration.ofDays(numberOfDays))))
            .fetch();
    }
}

