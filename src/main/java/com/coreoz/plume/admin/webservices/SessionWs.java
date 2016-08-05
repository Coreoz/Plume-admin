package com.coreoz.plume.admin.webservices;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.coreoz.plume.admin.services.time.TimeProvider;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.webservices.data.session.Credentials;
import com.coreoz.plume.admin.webservices.data.session.SessionBo;
import com.coreoz.plume.admin.webservices.errors.AdminWsError;
import com.coreoz.plume.admin.webservices.security.SessionBasicInformation;
import com.coreoz.plume.admin.webservices.security.SessionSigner;
import com.coreoz.plume.jersey.errors.WsException;
import com.typesafe.config.Config;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/admin/session")
@Api(value = "Manage the administration session")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class SessionWs {

	private final AdminUserService adminUserService;
	private final SessionSigner sessionSigner;
	private final TimeProvider timeProvider;

	private final long maxTimeSessionDurationInMilliseconds;

	@Inject
	public SessionWs(AdminUserService adminUserService,
			SessionSigner sessionSigner, TimeProvider timeProvider,
			Config config) {
		this.adminUserService = adminUserService;
		this.sessionSigner = sessionSigner;
		this.timeProvider = timeProvider;

		this.maxTimeSessionDurationInMilliseconds = config.getDuration("session-duration", TimeUnit.MILLISECONDS);
	}

	@POST
	@ApiOperation(value = "Authenticate a user and create a session token")
	public SessionBo authenticate(Credentials credentials) {
		return adminUserService
				.authenticate(credentials.getIdentifier(), credentials.getPassword())
				.map(user ->
					SessionBo.of(
						sessionSigner.serializeSession(
							new SessionBasicInformation()
								.setExpirationTime(timeProvider.currentTime() + maxTimeSessionDurationInMilliseconds)
								.setPermissions(user.getPermissions())
								.setUserId(user.getUser().getId())
								.setUsername(user.getUser().getUserName())
						),
						user.getUser().getFirstName() + " " + user.getUser().getLastName(),
						user.getPermissions()
					)
				)
				.orElseThrow(() -> new WsException(AdminWsError.WRONG_LOGIN_OR_PASSWORD));
	}

}
