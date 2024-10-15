package com.coreoz.plume.admin.db.daos;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.coreoz.plume.admin.db.generated.LogHeader;
import com.coreoz.plume.admin.db.generated.QLogHeader;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

@Singleton
public class LogHeaderDao extends CrudDaoQuerydsl<LogHeader> {

    @Inject
    public LogHeaderDao(TransactionManagerQuerydsl transactionManager) {
        super(transactionManager, QLogHeader.logHeader);
    }

    public List<LogHeader> findHeadersByApi(Long idLogApi, String httpPart) {
        return transactionManager
            .selectQuery()
            .select(QLogHeader.logHeader)
            .from(QLogHeader.logHeader)
            .where(QLogHeader.logHeader.idLogApi.eq(idLogApi).and(QLogHeader.logHeader.type.eq(httpPart)))
            .fetch();
    }

    public void deleteHeadersbyApi(Long idApi) {
        transactionManager
            .delete(QLogHeader.logHeader)
            .where(QLogHeader.logHeader.idLogApi.eq(idApi))
            .execute();
    }
}
