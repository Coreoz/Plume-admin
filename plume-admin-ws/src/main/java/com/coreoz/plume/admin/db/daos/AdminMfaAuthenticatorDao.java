package com.coreoz.plume.admin.db.daos;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.generated.AdminMfaAuthenticator;
import com.coreoz.plume.admin.db.generated.QAdminMfaAuthenticator;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

@Singleton
public class AdminMfaAuthenticatorDao extends CrudDaoQuerydsl<AdminMfaAuthenticator> {

    @Inject
	private AdminMfaAuthenticatorDao(TransactionManagerQuerydsl transactionManager) {
		super(transactionManager, QAdminMfaAuthenticator.adminMfaAuthenticator);
	}
}
