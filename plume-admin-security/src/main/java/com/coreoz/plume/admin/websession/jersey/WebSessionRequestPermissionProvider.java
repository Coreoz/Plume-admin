package com.coreoz.plume.admin.websession.jersey;

import java.util.Collection;

import jakarta.ws.rs.container.ContainerRequestContext;

import com.coreoz.plume.admin.websession.WebSessionFingerprint;
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
public class WebSessionRequestPermissionProvider<T extends WebSessionPermission & WebSessionFingerprint> implements PermissionRequestProvider {

	private final WebSessionSigner webSessionSigner;
	private final Class<T> webSessionClass;
	private final boolean verifyCookieFingerprint;

	public WebSessionRequestPermissionProvider(WebSessionSigner webSessionSigner, Class<T> webSessionClass,
			boolean verifyCookieFingerprint) {
		this.webSessionSigner = webSessionSigner;
		this.webSessionClass = webSessionClass;
		this.verifyCookieFingerprint = verifyCookieFingerprint;
	}

	@Override
	public String userInformation(ContainerRequestContext requestContext) {
		WebSessionPermission session = JerseySessionParser.currentSessionInformation(requestContext, webSessionSigner, webSessionClass, verifyCookieFingerprint);
		return session == null ? "<no user connected>" : session.getUserName();
	}

	@Override
	public Collection<String> correspondingPermissions(ContainerRequestContext requestContext) {
		WebSessionPermission session = JerseySessionParser.currentSessionInformation(requestContext, webSessionSigner, webSessionClass, verifyCookieFingerprint);
		return session == null || session.getPermissions() == null ?
			ImmutableList.of()
			: session.getPermissions();
	}

}
