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
import com.coreoz.plume.db.crud.CrudService;

@Singleton
public class AdminRoleService extends CrudService<AdminRole> {

	private final AdminRolePermissionDao adminRolePermissionDao;
	private final AdminPermissionService adminPermissionService;

	@Inject
	public AdminRoleService(AdminRoleDao adminRoleDao,
			AdminRolePermissionDao adminRolePermissionDao,
			AdminPermissionService adminPermissionService) {
		super(adminRoleDao);

		this.adminRolePermissionDao = adminRolePermissionDao;
		this.adminPermissionService = adminPermissionService;
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
						.roleId(rolePermissions.getKey())
						.permissions(
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

}
