package com.coreoz.plume.admin.db.daos;

import com.coreoz.plume.admin.db.generated.LogApi;
import com.coreoz.plume.admin.db.generated.QLogApi;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;



@Singleton
public class LogApiDao extends CrudDaoQuerydsl<LogApi> {

    @Inject
    public LogApiDaoC(TransactionManagerQuerydsl transactionManager) {
        super(transactionManager, QLogApi.logApi);
    }

    public List<LogApi> getLogsbyApiName(String apiName){
        return transactionManager
            .selectQuery()
            .select(QLogApi.logApi)
            .from(QLogApi.logApi)
            .where(QLogApi.logApi.api.eq(apiName))
            .fetch();
    }

    public List<String> getListApiNames(){
        return transactionManager
            .selectQuery()
            .select(QLogApi.logApi.api)
            .distinct()
            .from(QLogApi.logApi)
            .fetch();
    }

    public List<LogApi> getListApibyDate(int numberOfDays){
        return transactionManager
            .selectQuery()
            .select(QLogApi.logApi)
            .from(QLogApi.logApi)
            .where(QLogApi.logApi.date.before(Instant.now().minus(Duration.ofDays(numberOfDays))))
            .fetch();
    }
}

