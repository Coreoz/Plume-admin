package com.coreoz.plume.admin.services.session;

import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.websession.WebSessionAdmin;

/**
 * Default {@link WebSessionProvider} that creates {@link WebSessionAdmin} sessions.
 */
public class WebSessionAdminProvider implements WebSessionProvider<WebSessionAdmin> {

	@Override
	public Class<WebSessionAdmin> sessionClass() {
		return WebSessionAdmin.class;
	}

	@Override
	public WebSessionAdmin createSession(AuthenticatedUser user, String hashedFingerprint) {
		return new WebSessionAdmin()
			.setPermissions(user.getPermissions())
			.setIdUser(user.getUser().getId())
			.setUserName(user.getUser().getUserName())
			.setFullName(user.getUser().getFirstName() + " " + user.getUser().getLastName())
			.setHashedFingerprint(hashedFingerprint);
	}

}
