package com.coreoz.plume.admin.webservices.security;

import com.coreoz.plume.admin.security.permission.WebSessionPermission;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;

public interface WebSessionProvider {

	Class<? extends WebSessionPermission> webSessionClass();

	WebSessionPermission toWebSession(AuthenticatedUser user);

}
