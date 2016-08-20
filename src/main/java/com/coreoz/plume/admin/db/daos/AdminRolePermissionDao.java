package com.coreoz.plume.admin.db.daos;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import com.coreoz.plume.admin.db.entities.AdminRolePermission;
import com.coreoz.plume.admin.db.entities.AdminRolePermission.AdminRolePermissionId;
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
		return search(QAdminRolePermission.adminRolePermission.id.idRole.eq(idRole));
	}

	public void deleteForRole(long idRole, EntityManager em) {
		transactionManager
			.queryDsl(em)
			.delete(QAdminRolePermission.adminRolePermission)
			.where(QAdminRolePermission.adminRolePermission.id.idRole.eq(idRole))
			.execute();
	}

	public void addAll(long idRole, Set<String> permissions, EntityManager em) {
		for(String permission : permissions) {
			save(
				new AdminRolePermission().setId(
					new AdminRolePermissionId()
						.setIdRole(idRole)
						.setPermission(permission)
				),
				em
			);
		}
	}

}
