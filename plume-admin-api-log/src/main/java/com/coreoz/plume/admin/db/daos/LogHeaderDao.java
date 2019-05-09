package com.coreoz.plume.admin.db.daos;

import com.coreoz.plume.admin.db.generated.LogHeader;
import com.coreoz.plume.admin.db.generated.QLogHeader;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class LogHeaderDao extends CrudDaoQuerydsl<LogHeader> {

    @Inject
    public LogHeaderDao(TransactionManagerQuerydsl transactionManager) {
        super(transactionManager, QLogHeader.logHeader);
    }

    public List<LogHeader> findHeadersByApi(Long idApi, String type) {
        return transactionManager
            .selectQuery()
            .select(QLogHeader.logHeader)
            .from(QLogHeader.logHeader)
            .where(QLogHeader.logHeader.idLogApi.eq(idApi).and(QLogHeader.logHeader.type.eq(type)))
            .fetch();
    }

    public void deleteHeadersbyApi(Long idApi){
        transactionManager
            .delete(QLogHeader.logHeader)
            .where(QLogHeader.logHeader.idLogApi.eq(idApi))
            .execute();
    }
}
