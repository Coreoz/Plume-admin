package com.coreoz.plume.admin.websession.jersey;

import java.util.Collection;

import javax.ws.rs.container.ContainerRequestContext;

import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.jersey.security.permission.PermissionRequestProvider;
import com.google.common.collect.ImmutableList;

public class WebSessionRequestPermissionProvider<T extends WebSessionPermission>
	extends JerseyJwtSessionParser<T>
	implements PermissionRequestProvider {

	@SuppressWarnings("unchecked")
	public WebSessionRequestPermissionProvider(
			WebSessionSigner webSessionSigner, Class<T> webSessionClass) {
		super((JwtSessionSigner<T>) webSessionSigner, webSessionClass);
	}

	@Override
	public String userInformation(ContainerRequestContext requestContext) {
		WebSessionPermission session = currentSessionInformation(requestContext);
		return session == null ? "<no user connected>" : session.getUserName();
	}

	@Override
	public Collection<String> correspondingPermissions(ContainerRequestContext requestContext) {
		WebSessionPermission session = currentSessionInformation(requestContext);
		return session == null || session.getPermissions() == null ?
			ImmutableList.of()
			: session.getPermissions();
	}

}
