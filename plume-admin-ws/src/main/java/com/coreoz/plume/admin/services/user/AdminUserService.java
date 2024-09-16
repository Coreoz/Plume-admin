package com.coreoz.plume.admin.services.user;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.daos.AdminMfaDao;
import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.db.generated.AdminMfaAuthenticator;
import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.db.generated.AdminUserMfa;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.mfa.MfaService;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.webservices.data.user.AdminUserParameters;
import com.coreoz.plume.admin.webservices.validation.AdminWsError;
import com.coreoz.plume.db.crud.CrudService;
import com.coreoz.plume.jersey.errors.WsException;
import com.coreoz.plume.services.time.TimeProvider;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

@Singleton
public class AdminUserService extends CrudService<AdminUser> {

	private final AdminUserDao adminUserDao;
    private final AdminMfaDao adminMfaDao;
	private final AdminRoleService adminRoleService;
	private final HashService hashService;
    private final MfaService mfaService;
	private final TimeProvider timeProvider;

	@Inject
	public AdminUserService(AdminUserDao adminUserDao, AdminRoleService adminRoleService,
			HashService hashService, TimeProvider timeProvider, MfaService mfaService,
            AdminMfaDao adminMfaDao) {
		super(adminUserDao);

		this.adminUserDao = adminUserDao;
        this.adminMfaDao = adminMfaDao;
		this.adminRoleService = adminRoleService;
        this.mfaService = mfaService;
		this.hashService = hashService;
		this.timeProvider = timeProvider;
	}

	public Optional<AuthenticatedUser> authenticate(String userName, String password) {
		return adminUserDao
				.findByUserName(userName)
				.filter(user -> hashService.checkPassword(password, user.getPassword()))
				.map(user -> AuthenticatedUserAdmin.of(
					user,
					ImmutableSet.copyOf(adminRoleService.findRolePermissions(user.getIdRole()))
				));
	}

    public Optional<AuthenticatedUser> authenticateWithAuthenticator(String userName, int code) {
		return adminUserDao
				.findByUserName(userName)
				.filter(user -> {
                    List<AdminMfaAuthenticator> registeredAuthenticators = adminMfaDao.findAuthenticatorByUserId(user.getId());
                    // If any of the MFA is valid, then the user is valid
                    return registeredAuthenticators.stream().anyMatch(authenticator -> {
                        try {
                            return mfaService.verifyCode(authenticator.getSecretKey(), code);
                        } catch (Exception e) {
                            return false;
                        }
                    });
                })
				.map(user -> AuthenticatedUserAdmin.of(
					user,
					ImmutableSet.copyOf(adminRoleService.findRolePermissions(user.getIdRole()))
				));
	}

    public AuthenticatedUser authenticateWithMfa(AdminUser user) {
        return AuthenticatedUserAdmin.of(
            user,
            ImmutableSet.copyOf(adminRoleService.findRolePermissions(user.getIdRole()))
        );
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

    public String createMfaAuthenticatorSecretKey(Long idUser) throws Exception {
        AdminUser user = adminUserDao.findById(idUser);
        String secretKey = mfaService.generateSecretKey();
        AdminMfaAuthenticator mfa = new AdminMfaAuthenticator();
        mfa.setSecretKey(mfaService.hashSecretKey(secretKey));
        adminMfaDao.addMfaAuthenticatorToUser(user.getId(), mfa);
        adminUserDao.save(user);

        return secretKey;
    }

	public AdminUser create(AdminUserParameters parameters) {
		AdminUser adminUserToSave = new AdminUser();
		adminUserToSave.setIdRole(parameters.getIdRole());
		adminUserToSave.setCreationDate(timeProvider.currentDateTime());
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
