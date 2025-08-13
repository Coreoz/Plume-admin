package com.coreoz.plume.admin.services.user;

import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.webservices.data.user.AdminUserParameters;
import com.coreoz.plume.db.crud.CrudService;
import com.coreoz.securelogin.DelayedCompleter;
import com.coreoz.securelogin.TimingAttackProtector;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Singleton
public class AdminUserService extends CrudService<AdminUser> {

	private final AdminUserDao adminUserDao;
	private final AdminRoleService adminRoleService;
	private final HashService hashService;
    private final TimingAttackProtector timingAttackProtector;
    private final DelayedCompleter delayedCompleter;
    private final Clock clock;

	@Inject
	public AdminUserService(AdminUserDao adminUserDao, AdminRoleService adminRoleService,
                            HashService hashService, Clock clock, AdminConfigurationService configurationService) {
		super(adminUserDao);

		this.adminUserDao = adminUserDao;
		this.adminRoleService = adminRoleService;
		this.hashService = hashService;
        this.clock = clock;
        this.delayedCompleter = new DelayedCompleter();
        this.timingAttackProtector = new TimingAttackProtector(new TimingAttackProtector.Config(
            configurationService.loginTimeingProtectorMaxSamples(),
            configurationService.loginTimeingProtectorSamplingRate()
        ));
        // Initialize timing protector
        String dummyPassword = hashService.hashPassword("dummy password");
        this.timingAttackProtector.measureAndExecute(() -> hashService.checkPassword("wrong-dummy-password", dummyPassword));
    }

	public CompletableFuture<Optional<AuthenticatedUser>> authenticate(String userName, String password) {
        AdminUser foundUser = adminUserDao.findByUserName(userName).orElse(null);
        if (foundUser == null) {
            // The user is not found => wait for a random duration to simulate password verification
            return delayedCompleter.waitDuration(timingAttackProtector.generateDelay())
                // No user is returned
                .thenApply(unused -> Optional.empty());
        }

        // The user is found => verify the password and measure the time to do so
        Boolean authenticationSuccess = timingAttackProtector.measureAndExecute(() -> hashService.checkPassword(password, foundUser.getPassword()));
        if (!authenticationSuccess) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return CompletableFuture.completedFuture(Optional.of(
            AuthenticatedUserAdmin.of(
                foundUser,
                Set.copyOf(adminRoleService.findRolePermissions(foundUser.getIdRole()))
            )
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
