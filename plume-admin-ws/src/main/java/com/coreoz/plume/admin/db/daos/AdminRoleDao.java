package com.coreoz.plume.admin.db.daos;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.coreoz.plume.admin.db.generated.AdminRole;
import com.coreoz.plume.admin.db.generated.QAdminRole;
import com.coreoz.plume.admin.db.generated.QAdminRolePermission;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;
import com.querydsl.sql.SQLExpressions;

import lombok.Value;

@Singleton
public class AdminRoleDao extends CrudDaoQuerydsl<AdminRole> {

	@Inject
	public AdminRoleDao(TransactionManagerQuerydsl transactionManager) {
		super(transactionManager, QAdminRole.adminRole, QAdminRole.adminRole.label.asc());
	}

	public boolean existsWithLabel(Long idRole, String label) {
		return transactionManager
			.selectQuery()
			.select(SQLExpressions.selectOne())
			.from(QAdminRole.adminRole)
			.where(idRole != null ? QAdminRole.adminRole.id.ne(idRole) : null)
			.where(QAdminRole.adminRole.label.eq(label))
			.fetchOne() != null;
	}

	public List<RolePermissionDetails> findAllWithPermission() {
		return transactionManager
			.selectQuery()
			.select(
				QAdminRole.adminRole.id,
				QAdminRole.adminRole.label,
				QAdminRolePermission.adminRolePermission.permission
			)
			.from(QAdminRole.adminRole)
			.leftJoin(QAdminRolePermission.adminRolePermission)
			.on(QAdminRolePermission.adminRolePermission.idRole.eq(QAdminRole.adminRole.id))
			.fetch()
			.stream()
			.map(row -> RolePermissionDetails.of(
				row.get(QAdminRole.adminRole.id),
				row.get(QAdminRole.adminRole.label),
				row.get(QAdminRolePermission.adminRolePermission.permission)
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
