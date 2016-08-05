package com.coreoz.plume.admin.db.daos;

import java.util.Optional;

import javax.inject.Singleton;

import com.coreoz.plume.admin.db.entities.AdminUser;
import com.coreoz.plume.admin.db.entities.QAdminUser;
import com.coreoz.plume.db.TransactionManager;
import com.coreoz.plume.db.crud.CrudDao;

@Singleton
public class AdminUserDao extends CrudDao<AdminUser> {

	public AdminUserDao(TransactionManager transactionManager) {
		super(QAdminUser.adminUser, transactionManager, QAdminUser.adminUser.userName.asc());
	}

	public Optional<AdminUser> findByUserName(String userName) {
		return Optional.ofNullable(searchOne(QAdminUser.adminUser.userName.eq(userName)));
	}

}
