package com.coreoz.plume.admin.webservices;

import java.security.SecureRandom;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.coreoz.plume.admin.security.login.LoginFailAttemptsManager;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.webservices.data.session.AdminCredentials;
import com.coreoz.plume.admin.webservices.data.session.AdminSession;
import com.coreoz.plume.admin.webservices.validation.AdminWsError;
import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.jersey.JerseySessionParser;
import com.coreoz.plume.jersey.errors.Validators;
import com.coreoz.plume.jersey.errors.WsException;
import com.coreoz.plume.jersey.security.permission.PublicApi;
import com.coreoz.plume.services.time.TimeProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Path("/admin/session")
@Api(value = "Manage the administration session")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
// This API is marked as public, since it must be accessed without any authentication
@PublicApi
@Singleton
public class SessionWs {

	public static final FingerprintWithHash NULL_FINGERPRINT = new FingerprintWithHash(null, null);

	private final AdminUserService adminUserService;
	private final JwtSessionSigner jwtSessionSigner;
	private final TimeProvider timeProvider;
	private final LoginFailAttemptsManager failAttemptsManager;

	private final long blockedDurationInSeconds;

	private final long maxTimeSessionDurationInMilliseconds;
	private final long sessionRefreshDurationInMillis;
	private final long sessionInactiveDurationInMillis;

	private final SecureRandom fingerprintGenerator;
	private final boolean sessionUseFingerprintCookie;
	private final boolean sessionFingerprintCookieHttpsOnly;

	@Inject
	public SessionWs(AdminUserService adminUserService,
			JwtSessionSigner jwtSessionSigner,
			AdminConfigurationService configurationService,
			AdminSecurityConfigurationService adminSecurityConfigurationService,
			TimeProvider timeProvider) {
		this.adminUserService = adminUserService;
		this.jwtSessionSigner = jwtSessionSigner;
		this.timeProvider = timeProvider;

		this.failAttemptsManager = new LoginFailAttemptsManager(
			configurationService.loginMaxAttempts(),
			configurationService.loginBlockedDuration()
		);
		this.blockedDurationInSeconds = configurationService.loginBlockedDuration().getSeconds();
		this.maxTimeSessionDurationInMilliseconds = configurationService.sessionExpireDurationInMillis();
		this.sessionRefreshDurationInMillis = configurationService.sessionRefreshDurationInMillis();
		this.sessionInactiveDurationInMillis = configurationService.sessionInactiveDurationInMillis();

		this.fingerprintGenerator = new SecureRandom();
		this.sessionUseFingerprintCookie = adminSecurityConfigurationService.sessionUseFingerprintCookie();
		this.sessionFingerprintCookieHttpsOnly = adminSecurityConfigurationService.sessionFingerprintCookieHttpsOnly();
	}

	@POST
	@ApiOperation(value = "Authenticate a user and create a session token")
	public Response authenticate(AdminCredentials credentials) {
		// first user needs to be authenticated (an exception will be raised otherwise)
		AuthenticatedUser authenticatedUser = authenticateUser(credentials);
		// if the client is authenticated, the fingerprint can be generated if needed
		FingerprintWithHash fingerprintWithHash = sessionUseFingerprintCookie ? generateFingerprint() : NULL_FINGERPRINT;
		return withFingerprintCookie(
			Response.ok(toAdminSession(toWebSession(authenticatedUser, fingerprintWithHash.getHash()))),
			fingerprintWithHash.getFingerprint()
		)
		.build();
	}

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Renew a valid session token")
	public AdminSession renew(String webSessionSerialized) {
		Validators.checkRequired("sessionToken", webSessionSerialized);

		WebSessionAdmin parsedSession = jwtSessionSigner.parseSession(
			webSessionSerialized,
			WebSessionAdmin.class
		);

		if(parsedSession == null) {
			throw new WsException(AdminWsError.ALREADY_EXPIRED_SESSION_TOKEN);
		}

		return toAdminSession(parsedSession);
	}

	public AuthenticatedUser authenticateUser(AdminCredentials credentials) {
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
			.orElseThrow(() -> {
				failAttemptsManager.addAttempt(credentials.getUserName());
				return new WsException(AdminWsError.WRONG_LOGIN_OR_PASSWORD);
			});
	}

	public WebSessionPermission toWebSession(AuthenticatedUser user, String hashedFingerprint) {
		return new WebSessionAdmin()
			.setPermissions(user.getPermissions())
			.setIdUser(user.getUser().getId())
			.setUserName(user.getUser().getUserName())
			.setFullName(user.getUser().getFirstName() + " " + user.getUser().getLastName())
			.setHashedFingerprint(hashedFingerprint);
	}

	public AdminSession toAdminSession(WebSessionPermission webSession) {
		return new AdminSession(
			jwtSessionSigner.serializeSession(
				webSession,
				timeProvider.currentTime() + maxTimeSessionDurationInMilliseconds
			),
			sessionRefreshDurationInMillis,
			sessionInactiveDurationInMillis
		);
	}

	public ResponseBuilder withFingerprintCookie(ResponseBuilder response, String fingerprint) {
		return response.header(
			HttpHeaders.SET_COOKIE,
			JerseySessionParser.FINGERPRINT_COOKIE_NAME + "=" + fingerprint + "; SameSite=Strict; HttpOnly"
			+ (sessionFingerprintCookieHttpsOnly ? "; Secure" : "")
		);
	}

	public FingerprintWithHash generateFingerprint() {
		byte[] generatedFingerprintBytes = new byte[50];
		fingerprintGenerator.nextBytes(generatedFingerprintBytes);
		String generatedFingerprint = BaseEncoding.base16().encode(generatedFingerprintBytes);
		return new FingerprintWithHash(
			generatedFingerprint,
			JerseySessionParser.hashFingerprint(generatedFingerprint)
		);
	}

	@AllArgsConstructor
	@Getter
	public static class FingerprintWithHash {
		private final String fingerprint;
		private final String hash;
	}

}
