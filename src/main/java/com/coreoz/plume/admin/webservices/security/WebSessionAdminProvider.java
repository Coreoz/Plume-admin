package com.coreoz.plume.admin.webservices.security;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.security.permission.WebSessionPermission;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.time.TimeProvider;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;

@Singleton
public class WebSessionAdminProvider implements WebSessionProvider {

	private final TimeProvider timeProvider;
	private final long maxTimeSessionDurationInMilliseconds;

	@Inject
	public WebSessionAdminProvider(TimeProvider timeProvider,
			AdminConfigurationService configurationService) {
		this.timeProvider = timeProvider;
		this.maxTimeSessionDurationInMilliseconds = configurationService.sessionDurationInMillis();
	}

	@Override
	public Class<? extends WebSessionPermission> webSessionClass() {
		return WebSessionAdmin.class;
	}

	@Override
	public WebSessionPermission toWebSession(AuthenticatedUser user) {
		return new WebSessionAdmin()
			.setExpirationTime(timeProvider.currentTime() + maxTimeSessionDurationInMilliseconds)
			.setPermissions(user.getPermissions())
			.setIdUser(user.getUser().getId())
			.setUserName(user.getUser().getUserName())
			.setFullName(user.getUser().getFirstName() + " " + user.getUser().getLastName());
	}

}
