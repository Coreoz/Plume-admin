package com.coreoz.plume.admin.services.role;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.daos.AdminRoleDao;
import com.coreoz.plume.admin.db.daos.AdminRolePermissionDao;
import com.coreoz.plume.admin.db.entities.AdminRole;
import com.coreoz.plume.admin.db.entities.AdminRolePermission;
import com.coreoz.plume.db.crud.CrudService;

@Singleton
public class AdminRoleService extends CrudService<AdminRole> {

	private final AdminRolePermissionDao adminRolePermissionDao;

	@Inject
	public AdminRoleService(AdminRoleDao adminRoleDao, AdminRolePermissionDao adminRolePermissionDao) {
		super(adminRoleDao);

		this.adminRolePermissionDao = adminRolePermissionDao;
	}

	public Collection<String> findRolePermissions(Long idRole) {
		return adminRolePermissionDao
				.findRolePermissions(idRole)
				.stream()
				.map(AdminRolePermission::getPermission)
				.collect(Collectors.toList());
	}

}
