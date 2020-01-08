package com.coreoz.plume.admin.websession.jersey;

import java.util.Collection;

import javax.ws.rs.container.ContainerRequestContext;

import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.jersey.security.permission.PermissionFeature;
import com.coreoz.plume.jersey.security.permission.PermissionRequestProvider;
import com.google.common.collect.ImmutableList;

/**
 * Parse user information and permissions from a request. This is needed by {@link PermissionFeature}
 *
 * @param <T> A web session class implementing {@link WebSessionPermission}
 */
public class WebSessionRequestPermissionProvider<T extends WebSessionPermission> implements PermissionRequestProvider {

	private final WebSessionSigner webSessionSigner;
	private final Class<T> webSessionClass;

	public WebSessionRequestPermissionProvider(WebSessionSigner webSessionSigner, Class<T> webSessionClass) {
		this.webSessionSigner = webSessionSigner;
		this.webSessionClass = webSessionClass;
	}

	// TODO faire 2 méthodes statiques pour gérer la session + 2 implem de WebSessionRequestPermissionProvider

	@Override
	public String userInformation(ContainerRequestContext requestContext) {
		WebSessionPermission session = JerseySessionParser.currentSessionInformation(requestContext, webSessionSigner, webSessionClass);
		return session == null ? "<no user connected>" : session.getUserName();
	}

	@Override
	public Collection<String> correspondingPermissions(ContainerRequestContext requestContext) {
		WebSessionPermission session = JerseySessionParser.currentSessionInformation(requestContext, webSessionSigner, webSessionClass);
		return session == null || session.getPermissions() == null ?
			ImmutableList.of()
			: session.getPermissions();
	}

}
