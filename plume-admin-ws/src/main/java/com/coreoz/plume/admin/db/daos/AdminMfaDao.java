package com.coreoz.plume.admin.db.daos;

import java.util.List;

import javax.inject.Inject;

import com.coreoz.plume.admin.db.generated.AdminMfa;
import com.coreoz.plume.admin.db.generated.QAdminMfa;
import com.coreoz.plume.admin.db.generated.QAdminUserMfa;
import com.coreoz.plume.admin.services.mfa.MfaTypeEnum;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

public class AdminMfaDao extends CrudDaoQuerydsl<AdminMfa> {
    @Inject
	public AdminMfaDao(TransactionManagerQuerydsl transactionManager) {
		super(transactionManager, QAdminMfa.adminMfa);
	}

    public List<AdminMfa> findByUserId(long userId) {
		return selectFrom()
            .join(QAdminUserMfa.adminUserMfa)
            .on(QAdminUserMfa.adminUserMfa.idMfa.eq(QAdminMfa.adminMfa.id))
            .where(QAdminUserMfa.adminUserMfa.idUser.eq(userId))
            .fetch();
	}

    public List<AdminMfa> findMfaByUserIdAndType(long userId, MfaTypeEnum type) {
		return selectFrom()
            .join(QAdminUserMfa.adminUserMfa)
            .on(QAdminUserMfa.adminUserMfa.idMfa.eq(QAdminMfa.adminMfa.id))
            .where(QAdminUserMfa.adminUserMfa.idUser.eq(userId)
                .and(QAdminMfa.adminMfa.type.eq(type.getType())))
            .fetch();
	}

    public void addMfaToUser(long userId, AdminMfa mfa) {
        long mfaId = save(mfa).getId();
        transactionManager.insert(QAdminUserMfa.adminUserMfa)
            .set(QAdminUserMfa.adminUserMfa.idMfa, mfaId)
            .set(QAdminUserMfa.adminUserMfa.idUser, userId)
            .execute();
    }

    public void removeMfaFromUser(long userId, long mfaId) {
        AdminMfa mfa = findById(mfaId);
        if (mfa == null) {
            return;
        }
        transactionManager.delete(QAdminUserMfa.adminUserMfa)
            .where(QAdminUserMfa.adminUserMfa.idMfa.eq(mfaId)
                .and(QAdminUserMfa.adminUserMfa.idUser.eq(userId)))
            .execute();
        delete(mfaId);
    }
}
