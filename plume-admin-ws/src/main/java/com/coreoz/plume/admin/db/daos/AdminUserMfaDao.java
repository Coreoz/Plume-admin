package com.coreoz.plume.admin.db.daos;

import javax.inject.Singleton;

import com.coreoz.plume.admin.db.generated.QAdminUserMfa;
import com.coreoz.plume.admin.db.generated.AdminUserMfa;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;
import com.google.inject.Inject;

@Singleton
public class AdminUserMfaDao extends CrudDaoQuerydsl<AdminUserMfa> {

    @Inject
    private AdminUserMfaDao(TransactionManagerQuerydsl transactionManager) {
        super(transactionManager, QAdminUserMfa.adminUserMfa);
    }

    public AdminUserMfa findByUserIdAndMfaId(Long userId, Long mfaId) {
        return transactionManager
            .selectQuery()
            .select(QAdminUserMfa.adminUserMfa)
            .from(QAdminUserMfa.adminUserMfa)
            .where(QAdminUserMfa.adminUserMfa.idUser.eq(userId)
                .and(QAdminUserMfa.adminUserMfa.id.eq(mfaId)))
            .fetchOne();
    }
}
