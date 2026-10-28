package com.coreoz.plume.admin.services.session;

import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.websession.WebSessionAdmin;

/**
 * {@link WebSessionConfig} for the default {@link WebSessionAdmin} session type.
 */
public class AdminWebSessionConfig implements WebSessionConfig<WebSessionAdmin> {

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

	@Override
	public long authenticatedUserId(WebSessionAdmin parsedSession) {
		return parsedSession.getIdUser();
	}

}
