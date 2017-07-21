package com.coreoz.plume.admin.webservices.security;

import com.coreoz.plume.admin.jersey.feature.WebSessionClassProvider;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.websession.WebSessionPermission;

public interface WebSessionProvider extends WebSessionClassProvider {

	WebSessionPermission toWebSession(AuthenticatedUser user);

}
