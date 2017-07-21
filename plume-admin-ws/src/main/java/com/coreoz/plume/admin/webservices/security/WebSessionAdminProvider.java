package com.coreoz.plume.admin.webservices.security;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.websession.WebSessionPermission;

@Singleton
public class WebSessionAdminProvider implements WebSessionProvider {

	@Inject
	public WebSessionAdminProvider() {
	}

	@Override
	public Class<? extends WebSessionPermission> webSessionClass() {
		return WebSessionAdmin.class;
	}

	@Override
	public WebSessionPermission toWebSession(AuthenticatedUser user) {
		return new WebSessionAdmin()
			.setPermissions(user.getPermissions())
			.setIdUser(user.getUser().getId())
			.setUserName(user.getUser().getUserName())
			.setFullName(user.getUser().getFirstName() + " " + user.getUser().getLastName());
	}

}
