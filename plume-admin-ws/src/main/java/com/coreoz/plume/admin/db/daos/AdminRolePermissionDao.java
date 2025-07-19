package com.coreoz.plume.admin.db.daos;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.coreoz.plume.admin.db.generated.AdminRolePermission;
import com.coreoz.plume.admin.db.generated.QAdminRolePermission;
import com.coreoz.plume.db.querydsl.crud.QueryDslDao;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;
import com.querydsl.sql.dml.SQLInsertClause;

@Singleton
public class AdminRolePermissionDao extends QueryDslDao<AdminRolePermission> {

	@Inject
	public AdminRolePermissionDao(TransactionManagerQuerydsl transactionManager) {
		super(transactionManager, QAdminRolePermission.adminRolePermission);
	}

	public List<AdminRolePermission> findRolePermissions(Long idRole) {
		return selectFrom()
			.where(QAdminRolePermission.adminRolePermission.idRole.eq(idRole))
			.fetch();
	}

	public void deleteForRole(long idRole, Connection connection) {
		transactionManager
			.delete(QAdminRolePermission.adminRolePermission, connection)
			.where(QAdminRolePermission.adminRolePermission.idRole.eq(idRole))
			.execute();
	}

	public void addAll(long idRole, Set<String> permissions, Connection connection) {
		if(permissions.isEmpty()) {
			// a batch cannot be prepared with no value, else it raises an error
			// so if there is no permission to add, returns straightforward
			return;
		}

		SQLInsertClause inserts = transactionManager
			.insert(QAdminRolePermission.adminRolePermission, connection)
			.columns(
				QAdminRolePermission.adminRolePermission.idRole,
				QAdminRolePermission.adminRolePermission.permission
			);
		for(String permission : permissions) {
			inserts.values(idRole, permission).addBatch();
		}
		inserts.execute();
	}

}
