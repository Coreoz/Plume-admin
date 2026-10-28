package com.coreoz.plume.admin.webservices.session;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.coreoz.plume.admin.security.login.LoginFailAttemptsManager;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.session.AuthSessionTokenHandler;
import com.coreoz.plume.admin.services.session.WebSessionConfig;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.webservices.session.data.AdminCredentials;
import com.coreoz.plume.admin.webservices.session.data.AdminSession;
import com.coreoz.plume.admin.webservices.validation.AdminWsError;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.jersey.errors.ErrorResponse;
import com.coreoz.plume.jersey.errors.Validators;
import com.coreoz.plume.jersey.errors.WsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;

/**
 * Login workflow based on a user name and a password.
 * <p>
 * Extend this class (and provide a {@link WebSessionConfig}) to expose a password authentication
 * endpoint for a custom session type, for example:
 * <pre>
 * &#64;Path("/client/session")
 * &#64;Singleton
 * public class ClientSessionWs extends PasswordSessionWs&lt;ClientWebSession&gt; {
 *     &#64;Inject
 *     public ClientSessionWs(AdminUserService adminUserService,
 *             AdminConfigurationService configurationService,
 *             AuthSessionTokenHandler authSessionTokenHandler) {
 *         super(adminUserService, configurationService, authSessionTokenHandler, new ClientWebSessionConfig());
 *     }
 * }
 * </pre>
 * The token renewal endpoint is not provided: subclasses declare it themselves with
 * {@link #renewSession(String)}.
 *
 * @param <T> The web session type produced by this web-service
 */
public class PasswordSessionWs<T extends WebSessionPermission> extends SessionWsSupport<T> {

    private final AdminUserService adminUserService;
    private final LoginFailAttemptsManager failAttemptsManager;
    private final long blockedDurationInSeconds;

    protected PasswordSessionWs(
        AdminUserService adminUserService,
        AdminConfigurationService configurationService,
        AuthSessionTokenHandler authSessionTokenHandler,
        WebSessionConfig<T> sessionConfig
    ) {
        super(adminUserService, authSessionTokenHandler, sessionConfig);
        this.adminUserService = adminUserService;
        this.failAttemptsManager = new LoginFailAttemptsManager(
            configurationService.loginMaxAttempts(),
            configurationService.loginBlockedDuration()
        );
        this.blockedDurationInSeconds = configurationService.loginBlockedDuration().getSeconds();
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
            .thenAccept(authenticatedUser -> asyncResponse.resume(issueSession(authenticatedUser)))
            .exceptionally(error -> {
                // Exceptions caught here are likely CompletionException
                // In any case, we forward this exception to the asyncResponse
                // => It will then be caught and handled by Jersey ExceptionMapper, see WsResultExceptionMapper for details
                asyncResponse.resume(error);
                return null;
            });
    }

    /**
     * Checks the provided credentials, applying the brute-force protection.
     *
     * @throws WsException when the user is blocked or the credentials are invalid
     */
    protected CompletableFuture<AuthenticatedUser> authenticateUser(AdminCredentials credentials) {
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

}
