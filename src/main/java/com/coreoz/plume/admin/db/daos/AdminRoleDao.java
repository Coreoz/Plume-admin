package com.coreoz.plume.admin.db.daos;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.entities.AdminRole;
import com.coreoz.plume.admin.db.entities.QAdminRole;
import com.coreoz.plume.admin.db.entities.QAdminRolePermission;
import com.coreoz.plume.db.hibernate.TransactionManagerHibernate;
import com.coreoz.plume.db.hibernate.crud.CrudDaoHibernate;

import lombok.Value;

@Singleton
public class AdminRoleDao extends CrudDaoHibernate<AdminRole> {

	@Inject
	public AdminRoleDao(TransactionManagerHibernate transactionManager) {
		super(QAdminRole.adminRole, transactionManager, QAdminRole.adminRole.label.asc());
	}

	public boolean existsWithLabel(Long idRole, String label) {
		return searchCount(
			idRole != null ? QAdminRole.adminRole.id.ne(idRole) : null,
			QAdminRole.adminRole.label.eq(label)
		) > 0;
	}

	public List<RolePermissionDetails> findAllWithPermission() {
		return transactionManager.queryDslExecuteAndReturn(query ->
			query
				.select(
					QAdminRole.adminRole.id,
					QAdminRole.adminRole.label,
					QAdminRolePermission.adminRolePermission.id.permission
				)
				.from(QAdminRole.adminRole)
				.leftJoin(QAdminRolePermission.adminRolePermission)
				.on(QAdminRolePermission.adminRolePermission.id.idRole.eq(QAdminRole.adminRole.id))
				.fetch()
		)
		.stream()
		.map(row -> RolePermissionDetails.of(
			row.get(QAdminRole.adminRole.id),
			row.get(QAdminRole.adminRole.label),
			row.get(QAdminRolePermission.adminRolePermission.id.permission)
		))
		.collect(Collectors.toList());
	}

	@Value(staticConstructor = "of")
	public static class RolePermissionDetails {
		private final Long roleId;
		private final String roleLabel;
		private final String permission;
	}

}
