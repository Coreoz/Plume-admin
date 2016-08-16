package com.coreoz.plume.admin.webservices;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.coreoz.plume.admin.security.login.LoginFailAttemptsManager;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.webservices.data.session.AdminCredentials;
import com.coreoz.plume.admin.webservices.errors.AdminWsError;
import com.coreoz.plume.admin.webservices.security.WebSessionProvider;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.jersey.errors.Validators;
import com.coreoz.plume.jersey.errors.WsException;
import com.google.common.collect.ImmutableList;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/admin/session")
@Api(value = "Manage the administration session")
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class SessionWs {

	private final AdminUserService adminUserService;
	private final WebSessionSigner sessionSigner;
	private final WebSessionProvider webSessionProvider;

	private final LoginFailAttemptsManager failAttemptsManager;

	private final long blockedDurationInSeconds;

	@Inject
	public SessionWs(AdminUserService adminUserService,
			WebSessionSigner sessionSigner,
			AdminConfigurationService configurationService,
			WebSessionProvider webSessionProvider) {
		this.adminUserService = adminUserService;
		this.sessionSigner = sessionSigner;
		this.webSessionProvider = webSessionProvider;

		this.failAttemptsManager = new LoginFailAttemptsManager(
			configurationService.loginMaxAttempts(),
			configurationService.loginBlockedDuration()
		);
		this.blockedDurationInSeconds = configurationService.loginBlockedDuration().getSeconds();
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Authenticate a user and create a session token")
	public String authenticate(AdminCredentials credentials) {
		Validators.checkRequired("users.USERNAME", credentials.getUserName());
		Validators.checkRequired("users.PASSWORD", credentials.getPassword());

		if(credentials.getUserName() != null && failAttemptsManager.isBlocked(credentials.getUserName())) {
			throw new WsException(
				AdminWsError.TOO_MANY_WRONG_ATTEMPS,
				ImmutableList.of(String.valueOf(blockedDurationInSeconds))
			);
		}

		return adminUserService
			.authenticate(credentials.getUserName(), credentials.getPassword())
			.map(user ->
				sessionSigner.serializeSession(webSessionProvider.toWebSession(user))
			)
			.orElseThrow(() -> {
				failAttemptsManager.addAttempt(credentials.getUserName());
				return new WsException(AdminWsError.WRONG_LOGIN_OR_PASSWORD);
			});
	}

}
