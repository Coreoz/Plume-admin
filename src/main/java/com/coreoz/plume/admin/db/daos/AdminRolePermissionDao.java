package com.coreoz.plume.admin.db.daos;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.entities.AdminRolePermission;
import com.coreoz.plume.admin.db.entities.QAdminRolePermission;
import com.coreoz.plume.db.TransactionManager;
import com.coreoz.plume.db.crud.CrudDao;

@Singleton
public class AdminRolePermissionDao extends CrudDao<AdminRolePermission> {

	@Inject
	public AdminRolePermissionDao(TransactionManager transactionManager) {
		super(QAdminRolePermission.adminRolePermission, transactionManager);
	}

	public List<AdminRolePermission> findRolePermissions(Long idRole) {
		return search(QAdminRolePermission.adminRolePermission.idRole.eq(idRole));
	}

}
