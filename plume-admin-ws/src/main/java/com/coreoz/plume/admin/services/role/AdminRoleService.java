package com.coreoz.plume.admin.services.role;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.daos.AdminRoleDao;
import com.coreoz.plume.admin.db.daos.AdminRoleDao.RolePermissionDetails;
import com.coreoz.plume.admin.db.daos.AdminRolePermissionDao;
import com.coreoz.plume.admin.db.generated.AdminRole;
import com.coreoz.plume.admin.services.permissions.AdminPermissionService;
import com.coreoz.plume.db.crud.CrudService;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

@Singleton
public class AdminRoleService extends CrudService<AdminRole> {

	private final AdminRoleDao adminRoleDao;
	private final AdminRolePermissionDao adminRolePermissionDao;
	private final AdminPermissionService adminPermissionService;
	private final TransactionManagerQuerydsl transactionManager;

	@Inject
	public AdminRoleService(AdminRoleDao adminRoleDao,
			AdminRolePermissionDao adminRolePermissionDao,
			AdminPermissionService adminPermissionService,
			TransactionManagerQuerydsl transactionManager) {
		super(adminRoleDao);

		this.adminRoleDao = adminRoleDao;
		this.adminRolePermissionDao = adminRolePermissionDao;
		this.adminPermissionService = adminPermissionService;
		this.transactionManager = transactionManager;
	}

	public Collection<String> findRolePermissions(Long idRole) {
		return adminRolePermissionDao
				.findRolePermissions(idRole)
				.stream()
				.map(rolePermission -> rolePermission.getPermission())
				.collect(Collectors.toList());
	}

	public RolesAndPermissions findRoleWithPermissions() {
		return new RolesAndPermissions()
			.setPermissionsAvailable(adminPermissionService.permissionsAvailable())
			.setRolesWithPermissions(
				adminRoleDao
					.findAllWithPermission()
					.stream()
					.collect(Collectors.groupingBy(RolePermissionDetails::getRoleId))
					.entrySet()
					.stream()
					.map(rolePermissions -> new RoleWithPermissions()
						.setIdRole(rolePermissions.getKey())
						.setLabel(rolePermissions.getValue().get(0).getRoleLabel())
						.setPermissions(
							rolePermissions
								.getValue()
								.stream()
								.map(RolePermissionDetails::getPermission)
								.filter(permission -> permission != null)
								.collect(Collectors.toSet())
						)
					)
					.collect(Collectors.toList())
			);
	}

	public boolean existsWithLabel(Long idRole, String label) {
		return adminRoleDao.existsWithLabel(idRole, label);
	}

	public RoleWithPermissions saveWithPermissions(RoleWithPermissions roleWithPermissions) {
		return transactionManager.executeAndReturn(connection -> {
			AdminRole adminRoleToSave = new AdminRole();
			adminRoleToSave.setId(roleWithPermissions.getIdRole());
			adminRoleToSave.setLabel(roleWithPermissions.getLabel());

			AdminRole adminRoleSaved = adminRoleDao.save(adminRoleToSave, connection);

			if(roleWithPermissions.getIdRole() != null) {
				adminRolePermissionDao.deleteForRole(roleWithPermissions.getIdRole(), connection);
			}
			adminRolePermissionDao.addAll(
				adminRoleSaved.getId(),
				roleWithPermissions.getPermissions(),
				connection
			);

			return roleWithPermissions
				.setIdRole(adminRoleSaved.getId())
				.setLabel(adminRoleSaved.getLabel());
		});
	}

	public void deleteWithPermissions(long idRole) {
		transactionManager.execute(connection -> {
			adminRolePermissionDao.deleteForRole(idRole, connection);

			adminRoleDao.delete(idRole, connection);
		});
	}

}
