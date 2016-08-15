package com.coreoz.plume.admin.services.role;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.daos.AdminRoleDao;
import com.coreoz.plume.admin.db.daos.AdminRolePermissionDao;
import com.coreoz.plume.admin.db.entities.AdminRole;
import com.coreoz.plume.admin.db.entities.AdminRolePermission;
import com.coreoz.plume.admin.services.permissions.AdminPermissionService;
import com.coreoz.plume.db.TransactionManager;
import com.coreoz.plume.db.crud.CrudService;

@Singleton
public class AdminRoleService extends CrudService<AdminRole> {

	private final AdminRoleDao adminRoleDao;
	private final AdminRolePermissionDao adminRolePermissionDao;
	private final AdminPermissionService adminPermissionService;
	private final TransactionManager transactionManager;

	@Inject
	public AdminRoleService(AdminRoleDao adminRoleDao,
			AdminRolePermissionDao adminRolePermissionDao,
			AdminPermissionService adminPermissionService,
			TransactionManager transactionManager) {
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
				.map(AdminRolePermission::getPermission)
				.collect(Collectors.toList());
	}

	public RolesAndPermissions findRoleWithPermissions() {
		return new RolesAndPermissions()
			.permissionsAvailable(adminPermissionService.permissionsAvailable())
			.rolesWithPermissions(
				adminRolePermissionDao
					.findAll()
					.stream()
					.collect(Collectors.groupingBy(AdminRolePermission::getIdRole))
					.entrySet()
					.stream()
					.map(rolePermissions -> new RoleWithPermissions()
						.setIdRole(rolePermissions.getKey())
						.setPermissions(
							rolePermissions
								.getValue()
								.stream()
								.map(AdminRolePermission::getPermission)
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
		return transactionManager.executeAndReturn(em -> {
			AdminRole adminRoleSaved = adminRoleDao.save(
				new AdminRole()
					.setId(roleWithPermissions.getIdRole())
					.setLabel(roleWithPermissions.getLabel()),
				em
			);

			if(roleWithPermissions.getIdRole() != null) {
				adminRolePermissionDao.deleteForRole(roleWithPermissions.getIdRole(), em);
			}
			adminRolePermissionDao.addAll(
				adminRoleSaved.getId(),
				roleWithPermissions.getPermissions(),
				em
			);

			return roleWithPermissions
					.setIdRole(adminRoleSaved.getId())
					.setLabel(adminRoleSaved.getLabel());
		});
	}

	public void deleteWithPermissions(long idRole) {
		transactionManager.execute(em -> {
			adminRolePermissionDao.deleteForRole(idRole, em);

			adminRoleDao.delete(idRole, em);
		});
	}

}
