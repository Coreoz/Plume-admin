package com.coreoz.plume.admin.services.user;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.db.entities.AdminUser;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.db.crud.CrudService;
import com.google.common.collect.ImmutableSet;

@Singleton
public class AdminUserService extends CrudService<AdminUser> {

	private final AdminUserDao adminUserDao;
	private final AdminRoleService adminRoleService;
	private final HashService hashService;

	@Inject
	public AdminUserService(AdminUserDao adminUserDao, AdminRoleService adminRoleService, HashService hashService) {
		super(adminUserDao);

		this.adminUserDao = adminUserDao;
		this.adminRoleService = adminRoleService;
		this.hashService = hashService;
	}

	public Optional<AuthenticatedUser> authenticate(String userName, String password) {
		return adminUserDao
				.findByUserName(userName)
				.filter(user -> hashService.checkPassword(password, user.getPassword()))
				.map(user -> AuthenticatedUser.of(
					user,
					ImmutableSet.copyOf(adminRoleService.findRolePermissions(user.getIdRole()))
				));
	}

}
