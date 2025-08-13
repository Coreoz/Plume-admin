package com.coreoz.plume.admin.webservices;

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
import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Path("/admin/session")
@Tag(name = "admin-session", description = "Manage the administration session")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
// This API is marked as public, since it must be accessed without any authentication
@PublicApi
@Singleton
public class SessionWs {

	public static final FingerprintWithHash NULL_FINGERPRINT = new FingerprintWithHash(null, null);

	private final AdminUserService adminUserService;
	private final JwtSessionSigner jwtSessionSigner;
	private final Clock clock;
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
			Clock clock) {
		this.adminUserService = adminUserService;
		this.jwtSessionSigner = jwtSessionSigner;
		this.clock = clock;

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
	@Operation(description = "Authenticate a user and create a session token")
	public void authenticate(@Suspended final AsyncResponse asyncResponse, AdminCredentials credentials) {
		// First, the user needs to be authenticated (an exception will be raised otherwise)
		authenticateUser(credentials)
			.thenAccept(authenticatedUser -> {
				// if the client is authenticated, the fingerprint can be generated if needed
				FingerprintWithHash fingerprintWithHash = sessionUseFingerprintCookie ? generateFingerprint() : NULL_FINGERPRINT;
				asyncResponse.resume(
					withFingerprintCookie(
						Response.ok(toAdminSession(toWebSession(authenticatedUser, fingerprintWithHash.getHash()))),
						fingerprintWithHash.getFingerprint()
					)
					.build()
				);
			})
			.exceptionally(error -> {
                // Exceptions caught here are likely CompletionException
                // In any case, we forward this exception to the asyncResponse
                // => It will then be caught and handled by Jersey ExceptionMapper, see WsResultExceptionMapper for details
				asyncResponse.resume(error);
				return null;
			});
	}

	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	@Operation(description = "Renew a valid session token")
	public AdminSession renew(String webSessionSerialized) {
		Validators.checkRequired("sessionToken", webSessionSerialized);

		WebSessionAdmin parsedSession = jwtSessionSigner.parseSession(
			webSessionSerialized,
			WebSessionAdmin.class
		);

		if (parsedSession == null) {
			throw new WsException(AdminWsError.ALREADY_EXPIRED_SESSION_TOKEN);
		}

        // For each token renewal request, user information is verified again
		Optional<AuthenticatedUser> authenticatedUser = adminUserService.findAuthenticatedUserById(parsedSession.getIdUser());

		if (authenticatedUser.isEmpty()) {
			throw new WsException(AdminWsError.REQUEST_INVALID);
		}

		return toAdminSession(toWebSession(authenticatedUser.get(), parsedSession.getHashedFingerprint()));
	}

	public CompletableFuture<AuthenticatedUser> authenticateUser(AdminCredentials credentials) {
		Validators.checkRequired("Json creadentials", credentials);
		Validators.checkRequired("users.USERNAME", credentials.getUserName());
		Validators.checkRequired("users.PASSWORD", credentials.getPassword());

		if(credentials.getUserName() != null && failAttemptsManager.isBlocked(credentials.getUserName())) {
			throw new WsException(
				AdminWsError.TOO_MANY_WRONG_ATTEMPS,
				List.of(String.valueOf(blockedDurationInSeconds))
			);
		}

		return adminUserService
			.authenticate(credentials.getUserName(), credentials.getPassword())
			.thenApply(authenticatedUser -> authenticatedUser.orElseThrow(() -> {
				failAttemptsManager.addAttempt(credentials.getUserName());
				return new WsException(AdminWsError.WRONG_LOGIN_OR_PASSWORD);
			}));
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
                clock.millis() + maxTimeSessionDurationInMilliseconds
			),
			sessionRefreshDurationInMillis,
			sessionInactiveDurationInMillis
		);
	}

	public ResponseBuilder withFingerprintCookie(ResponseBuilder response, String fingerprint) {
		return response.header(
			HttpHeaders.SET_COOKIE,
			JerseySessionParser.FINGERPRINT_COOKIE_NAME + "=" + fingerprint + "; path=/; SameSite=Strict; HttpOnly"
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
