package com.coreoz.plume.admin.services.session;

import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.websession.WebSessionPermission;

/**
 * Describes a web session type: how to create it and how to extract the authenticated user from it.
 * <p>
 * A config is not a service: it is created once by the web-service that exposes the session
 * (see {@code SessionWsSupport}) and passed to the {@link AuthSessionTokenHandler}. It is never
 * injected by the DI container, so no {@code TypeLiteral} binding is needed.
 *
 * @param <T> The web session type
 */
public interface WebSessionConfig<T extends WebSessionPermission> {

	Class<T> sessionClass();

	/**
	 * Creates the session object that will be serialized into the token.
	 *
	 * @param hashedFingerprint The hashed fingerprint, or {@code null} when the fingerprint cookie is disabled
	 */
	T createSession(AuthenticatedUser user, String hashedFingerprint);

	/**
	 * Extracts the authenticated user id from a parsed session.
	 * <p>
	 * Used on renewal to verify that the user still exists and is still active.
	 */
	long authenticatedUserId(T parsedSession);

}
