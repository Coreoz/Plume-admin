package com.coreoz.plume.admin.webservices;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.coreoz.plume.admin.security.login.LoginFailAttemptsManager;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.services.session.WebSessionProvider;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.webservices.data.session.AdminCredentials;
import com.coreoz.plume.admin.webservices.data.session.AdminSession;
import com.coreoz.plume.admin.webservices.validation.AdminWsError;
import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.plume.admin.websession.WebSessionFingerprint;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.jersey.JerseySessionParser;
import com.coreoz.plume.jersey.errors.ErrorResponse;
import com.coreoz.plume.jersey.errors.Validators;
import com.coreoz.plume.jersey.errors.WsError;
import com.coreoz.plume.jersey.errors.WsException;
import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Shared implementation of the session login/renew workflow.
 * <p>
 * Extend this class (and provide a {@link WebSessionProvider}) to expose the same workflow with a
 * custom web session type. Subclasses only need to add the JAX-RS {@code @Path} and class level
 * annotations, for example:
 * <pre>
 * &#64;Path("/client/session")
 * &#64;Singleton
 * public class ClientSessionWs extends AbstractSessionWs&lt;ClientWebSession&gt; {
 *     &#64;Inject
 *     public ClientSessionWs(..., WebSessionProvider&lt;ClientWebSession&gt; provider) {
 *         super(..., provider);
 *     }
 * }
 * </pre>
 *
 * @param <T> The web session type produced by this web-service
 */
public abstract class AbstractSessionWs<T extends WebSessionPermission> {

    public static final FingerprintWithHash NULL_FINGERPRINT = new FingerprintWithHash(null, null);

    private final AdminUserService adminUserService;
    private final JwtSessionSigner jwtSessionSigner;
    private final Clock clock;
    private final LoginFailAttemptsManager failAttemptsManager;
    private final WebSessionProvider<T> webSessionProvider;

    private final long blockedDurationInSeconds;

    private final long maxTimeSessionDurationInMilliseconds;
    private final long sessionRefreshDurationInMillis;
    private final long sessionInactiveDurationInMillis;

    private final SecureRandom fingerprintGenerator;
    private final boolean sessionUseFingerprintCookie;
    private final boolean sessionFingerprintCookieHttpsOnly;

    protected AbstractSessionWs(
        AdminUserService adminUserService,
        JwtSessionSigner jwtSessionSigner,
        AdminConfigurationService configurationService,
        AdminSecurityConfigurationService adminSecurityConfigurationService,
        Clock clock,
        WebSessionProvider<T> webSessionProvider
    ) {
        this.adminUserService = adminUserService;
        this.jwtSessionSigner = jwtSessionSigner;
        this.clock = clock;
        this.webSessionProvider = webSessionProvider;

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

    public WebSessionProvider<T> webSessionProvider() {
        return webSessionProvider;
    }

    public Class<T> sessionClass() {
        return webSessionProvider.sessionClass();
    }

    @POST
    @Operation(description = "Authenticate a user and create a session token")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication Successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AdminSession.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    // Async API (marked by using the Suspended annotation and the AsyncResponse argument) returns void,
    // the response will be written directly in the AsyncResponse argument
    public void authenticate(@Suspended final AsyncResponse asyncResponse, AdminCredentials credentials) {
        // First, the user needs to be authenticated (an exception will be raised otherwise)
        authenticateUser(credentials)
            .thenAccept(authenticatedUser -> {
                // if the client is authenticated, the fingerprint can be generated if needed
                FingerprintWithHash fingerprintWithHash = sessionUseFingerprintCookie ? generateFingerprint() : NULL_FINGERPRINT;
                asyncResponse.resume(
                    withFingerprintCookie(
                        Response.ok(toAdminSession(createSession(authenticatedUser, fingerprintWithHash.getHash()))),
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

        T parsedSession = jwtSessionSigner.parseSession(
            webSessionSerialized,
            sessionClass()
        );

        if (parsedSession == null) {
            throw new WsException(AdminWsError.ALREADY_EXPIRED_SESSION_TOKEN);
        }

        // For each token renewal request, user information is verified again
        Optional<AuthenticatedUser> authenticatedUser = adminUserService.findAuthenticatedUserById(authenticatedUserId(parsedSession));

        if (authenticatedUser.isEmpty()) {
            throw new WsException(WsError.REQUEST_INVALID, "Invalid session token");
        }

        return toAdminSession(createSession(authenticatedUser.get(), hashedFingerprint(parsedSession)));
    }

    public CompletableFuture<AuthenticatedUser> authenticateUser(AdminCredentials credentials) {
        Validators.checkRequired("Json credentials", credentials);
        Validators.checkRequired("users.USERNAME", credentials.getUserName());
        Validators.checkRequired("users.PASSWORD", credentials.getPassword());

        if (credentials.getUserName() != null && failAttemptsManager.isBlocked(credentials.getUserName())) {
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

	public T createSession(AuthenticatedUser user, String hashedFingerprint) {
		return webSessionProvider.createSession(user, hashedFingerprint);
	}

	/**
	 * @deprecated Use {@link #createSession(AuthenticatedUser, String)} instead.
	 */
	@Deprecated(forRemoval = true)
	public WebSessionPermission toWebSession(AuthenticatedUser user, String hashedFingerprint) {
		return createSession(user, hashedFingerprint);
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

    /**
     * Extract the user id from the parsed session.
     * <p>
     * The default implementation expects the session to be a
     * {@link com.coreoz.plume.admin.websession.WebSessionAdmin}. Override this method when using a
     * custom session type.
     */
    protected long authenticatedUserId(T parsedSession) {
        if (parsedSession instanceof WebSessionAdmin webSessionAdmin) {
            return webSessionAdmin.getIdUser();
        }
        throw new IllegalStateException(
            "Cannot find the authenticated user id from " + parsedSession.getClass()
                + ". Override AbstractSessionWs.authenticatedUserId() to support a custom session type."
        );
    }

    /**
     * Extract the hashed fingerprint from the parsed session.
     * <p>
     * The default implementation expects the session to be a
     * {@link com.coreoz.plume.admin.websession.WebSessionFingerprint}. Override this method when
     * using a custom session type.
     */
    protected String hashedFingerprint(T parsedSession) {
        if (parsedSession instanceof WebSessionFingerprint webSessionFingerprint) {
            return webSessionFingerprint.getHashedFingerprint();
        }
        return null;
    }

    @AllArgsConstructor
    @Getter
    public static class FingerprintWithHash {
        private final String fingerprint;
        private final String hash;
    }
}
