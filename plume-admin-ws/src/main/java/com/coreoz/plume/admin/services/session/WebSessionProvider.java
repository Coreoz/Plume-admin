package com.coreoz.plume.admin.services.session;

import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.websession.WebSessionPermission;

/**
 * Creates the web session object that will be serialized into the JWT token on login/renew.
 * <p>
 * Implement this interface to plug a custom session object into {@code AbstractSessionWs}.
 *
 * @param <T> The web session type
 */
public interface WebSessionProvider<T extends WebSessionPermission> {

	Class<T> sessionClass();

	T createSession(AuthenticatedUser user, String hashedFingerprint);

}
