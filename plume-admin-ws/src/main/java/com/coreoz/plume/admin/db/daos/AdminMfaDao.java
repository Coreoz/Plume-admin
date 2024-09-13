package com.coreoz.plume.admin.db.daos;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.generated.AdminMfaAuthenticator;
import com.coreoz.plume.admin.db.generated.AdminMfaBrowser;
import com.coreoz.plume.admin.db.generated.AdminUserMfa;
import com.coreoz.plume.admin.db.generated.QAdminMfaAuthenticator;
import com.coreoz.plume.admin.db.generated.QAdminMfaBrowser;
import com.coreoz.plume.admin.db.generated.QAdminUserMfa;
import com.coreoz.plume.admin.services.mfa.MfaTypeEnum;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

@Singleton
public class AdminMfaDao  {

    private final TransactionManagerQuerydsl transactionManager;
    private final AdminMfaAuthenticatorDao adminMfaAuthenticatorDao;
    private final AdminUserMfaDao adminUserMfaDao;
    private final AdminMfaBrowserCredentialDao adminMfaBrowserCredentialDao;

    @Inject
	private AdminMfaDao(
        TransactionManagerQuerydsl transactionManager,
        AdminMfaAuthenticatorDao adminMfaAuthenticatorDao,
        AdminUserMfaDao adminUserMfaDao,
        AdminMfaBrowserCredentialDao adminMfaBrowserCredentialDao
    ) {
		this.transactionManager = transactionManager;
        this.adminMfaAuthenticatorDao = adminMfaAuthenticatorDao;
        this.adminUserMfaDao = adminUserMfaDao;
        this.adminMfaBrowserCredentialDao = adminMfaBrowserCredentialDao;
	}

    public List<AdminMfaAuthenticator> findAuthenticatorByUserId(long userId) {
		return transactionManager.selectQuery()
            .select(QAdminMfaAuthenticator.adminMfaAuthenticator)
            .from(QAdminMfaAuthenticator.adminMfaAuthenticator)
            .join(QAdminUserMfa.adminUserMfa)
            .on(QAdminUserMfa.adminUserMfa.idMfaAuthenticator.eq(QAdminMfaAuthenticator.adminMfaAuthenticator.id))
            .where(QAdminUserMfa.adminUserMfa.idUser.eq(userId)
                .and(QAdminUserMfa.adminUserMfa.type.eq(MfaTypeEnum.AUTHENTICATOR.getType())))
            .fetch();
	}

    public List<AdminMfaBrowser> findMfaBrowserByUserId(long userId) {
		return transactionManager.selectQuery()
            .select(QAdminMfaBrowser.adminMfaBrowser)
            .from(QAdminMfaBrowser.adminMfaBrowser)
            .join(QAdminUserMfa.adminUserMfa)
            .on(QAdminUserMfa.adminUserMfa.idMfaBrowser.eq(QAdminMfaBrowser.adminMfaBrowser.id))
            .where(QAdminUserMfa.adminUserMfa.idUser.eq(userId)
                .and(QAdminUserMfa.adminUserMfa.type.eq(MfaTypeEnum.BROWSER.getType())))
            .fetch();
	}

    public void addMfaAuthenticatorToUser(long userId, AdminMfaAuthenticator mfa) {
        long mfaId = adminMfaAuthenticatorDao.save(mfa).getId();
        AdminUserMfa userMfa = new AdminUserMfa();
        userMfa.setIdUser(userId);
        userMfa.setIdMfaAuthenticator(mfaId);
        userMfa.setType(MfaTypeEnum.AUTHENTICATOR.getType());
        adminUserMfaDao.save(userMfa);
    }

    public void removeMfaAuthenticatorFromUser(long userId, long mfaId) {
        AdminMfaAuthenticator mfa = adminMfaAuthenticatorDao.findById(mfaId);
        if (mfa == null) {
            return;
        }
        AdminUserMfa userMfa = adminUserMfaDao.findByUserIdAndMfaId(userId, mfaId);
        adminUserMfaDao.delete(userMfa.getId());
        adminMfaAuthenticatorDao.delete(mfa.getId());
    }
}
