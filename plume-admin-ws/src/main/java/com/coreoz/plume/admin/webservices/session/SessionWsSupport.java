package com.coreoz.plume.admin.webservices.session;

import java.util.Optional;

import com.coreoz.plume.admin.services.session.AuthSessionTokenHandler;
import com.coreoz.plume.admin.services.session.WebSessionConfig;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.webservices.validation.AdminWsError;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.jersey.errors.Validators;
import com.coreoz.plume.jersey.errors.WsError;
import com.coreoz.plume.jersey.errors.WsException;
import jakarta.ws.rs.core.Response;

/**
 * Shared session primitives: issue a token and renew it.
 * <p>
 * This class carries no authentication logic and declares no JAX-RS endpoint: it only provides
 * {@link #issueSession(AuthenticatedUser)} and {@link #renewSession(String)} to subclasses, which
 * are free to expose their own contract (paths, verbs, annotations). This is what makes it
 * reusable by any authentication mode (password, OIDC, ...).
 *
 * @param <T> The web session type produced by the web-service
 */
public class SessionWsSupport<T extends WebSessionPermission> {

    private final AdminUserService adminUserService;
    private final AuthSessionTokenHandler authSessionTokenHandler;
    private final WebSessionConfig<T> sessionConfig;

    protected SessionWsSupport(
        AdminUserService adminUserService,
        AuthSessionTokenHandler authSessionTokenHandler,
        WebSessionConfig<T> sessionConfig
    ) {
        this.adminUserService = adminUserService;
        this.authSessionTokenHandler = authSessionTokenHandler;
        this.sessionConfig = sessionConfig;
    }

    /**
     * Creates a brand new session (token + fingerprint cookie) for the authenticated user.
     */
    protected Response issueSession(AuthenticatedUser user) {
        return authSessionTokenHandler.generateUserSession(user, sessionConfig);
    }

    /**
     * Validates the given token, checks that the user is still active and issues a new session.
     *
     * @throws WsException when the token is invalid, expired or the user is no longer active
     */
    protected Response renewSession(String webSessionSerialized) {
        Validators.checkRequired("sessionToken", webSessionSerialized);

        T parsedSession = authSessionTokenHandler
            .readUserSession(webSessionSerialized, sessionConfig)
            .orElseThrow(() -> new WsException(AdminWsError.ALREADY_EXPIRED_SESSION_TOKEN));

        // For each token renewal request, user information is verified again
        Optional<AuthenticatedUser> authenticatedUser = adminUserService
            .findAuthenticatedUserById(sessionConfig.authenticatedUserId(parsedSession));

        if (authenticatedUser.isEmpty()) {
            throw new WsException(WsError.REQUEST_INVALID, "Invalid session token");
        }

        return issueSession(authenticatedUser.get());
    }

    public Class<T> sessionClass() {
        return sessionConfig.sessionClass();
    }

    public AuthSessionTokenHandler authSessionTokenHandler() {
        return authSessionTokenHandler;
    }

    public WebSessionConfig<T> sessionConfig() {
        return sessionConfig;
    }

}
