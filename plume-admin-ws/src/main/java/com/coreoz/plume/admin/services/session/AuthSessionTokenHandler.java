package com.coreoz.plume.admin.services.session;

import java.util.Optional;

import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import jakarta.ws.rs.core.Response;

/**
 * Creates and reads the session token exchanged with the client.
 * <p>
 * This handler owns everything related to the token representation: serialization, signature,
 * expiration, and (optionally) the fingerprint cookie. The web-service layer only deals with
 * {@link AuthenticatedUser} and parsed sessions.
 * <p>
 * The handler is not generic: the session type is provided by the {@link WebSessionConfig}
 * argument, so a single instance can serve any session type and it can be injected directly,
 * without any {@code TypeLiteral} binding.
 * <p>
 * Use {@link JwtAuthSessionTokenHandler} for the default JWT + fingerprint cookie implementation.
 */
public interface AuthSessionTokenHandler {

	/**
	 * Creates a new session for the authenticated user and builds the complete HTTP response,
	 * including the token and, when enabled, the fingerprint cookie.
	 * <p>
	 * Used both on login and on token renewal: each call issues a brand new token and fingerprint.
	 *
	 * @param user   The authenticated user
	 * @param config The session type configuration
	 */
	<T extends WebSessionPermission> Response generateUserSession(AuthenticatedUser user, WebSessionConfig<T> config);

	/**
	 * Reads and validates a serialized session.
	 *
	 * @param webSessionSerialized The token sent by the client
	 * @param config              The session type configuration
	 * @return the parsed session, or {@link Optional#empty()} when the token is invalid or expired
	 */
	<T extends WebSessionPermission> Optional<T> readUserSession(String webSessionSerialized, WebSessionConfig<T> config);

}
