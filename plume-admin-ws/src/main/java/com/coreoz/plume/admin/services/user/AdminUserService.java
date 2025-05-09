package com.coreoz.plume.admin.services.user;

import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.webservices.data.user.AdminUserParameters;
import com.coreoz.plume.db.crud.CrudService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Singleton
public class AdminUserService extends CrudService<AdminUser> {

	private final AdminUserDao adminUserDao;
	private final AdminRoleService adminRoleService;
	private final HashService hashService;
	private final Clock clock;

	@Inject
	public AdminUserService(AdminUserDao adminUserDao, AdminRoleService adminRoleService,
			HashService hashService, Clock clock) {
		super(adminUserDao);

		this.adminUserDao = adminUserDao;
		this.adminRoleService = adminRoleService;
		this.hashService = hashService;
		this.clock = clock;
	}

	public Optional<AuthenticatedUser> authenticate(String userName, String password) {
		return adminUserDao
				.findByUserName(userName)
				.filter(user -> hashService.checkPassword(password, user.getPassword()))
				.map(user -> AuthenticatedUserAdmin.of(
					user,
					Set.copyOf(adminRoleService.findRolePermissions(user.getIdRole()))
				));
	}

    public Optional<AuthenticatedUser> findAuthenticatedUserById(long id) {
        return Optional.ofNullable(adminUserDao.findById(id))
            .map(user -> AuthenticatedUserAdmin.of(
                user,
                Set.copyOf(adminRoleService.findRolePermissions(user.getIdRole()))
            ));
    }

	public void update(AdminUserParameters parameters) {
		String newPassword = Strings.emptyToNull(parameters.getPassword());
		adminUserDao.update(
			parameters.getId(),
			parameters.getIdRole(),
			parameters.getUserName(),
			parameters.getEmail(),
			parameters.getFirstName(),
			parameters.getLastName(),
			newPassword == null ? null : hashService.hashPassword(newPassword)
		);
	}

	public AdminUser create(AdminUserParameters parameters) {
		AdminUser adminUserToSave = new AdminUser();
		adminUserToSave.setIdRole(parameters.getIdRole());
		adminUserToSave.setCreationDate(LocalDateTime.now(clock));
		adminUserToSave.setUserName(parameters.getUserName());
		adminUserToSave.setEmail(parameters.getEmail());
		adminUserToSave.setFirstName(parameters.getFirstName());
		adminUserToSave.setLastName(parameters.getLastName());
		adminUserToSave.setPassword(hashService.hashPassword(parameters.getPassword()));

		return adminUserDao.save(adminUserToSave);
	}

	public boolean existsWithUsername(Long idUser, String newUserName) {
		return adminUserDao.existsWithUsername(idUser, newUserName);
	}

	public boolean existsWithEmail(Long idUser, String newUserEmail) {
		return adminUserDao.existsWithEmail(idUser, newUserEmail);
	}

}
