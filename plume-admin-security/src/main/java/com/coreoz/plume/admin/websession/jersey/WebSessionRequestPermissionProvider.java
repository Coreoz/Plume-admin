package com.coreoz.plume.admin.websession.jersey;

import java.util.Collection;

import javax.ws.rs.container.ContainerRequestContext;

import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.jersey.security.permission.PermissionRequestProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;

public class WebSessionRequestPermissionProvider<T extends WebSessionPermission> implements PermissionRequestProvider {

	private static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";

	private static final String BEARER_PREFIX = "Bearer ";
	private static final Object EMPTY_SESSION = new Object();

	private final WebSessionSigner webSessionSigner;
	private final Class<T> webSessionClass;

	public WebSessionRequestPermissionProvider(
			WebSessionSigner webSessionSigner, Class<T> webSessionClass) {
		this.webSessionSigner = webSessionSigner;
		this.webSessionClass = webSessionClass;
	}

	@Override
	public String userInformation(ContainerRequestContext requestContext) {
		WebSessionPermission session = currentSessionInformation(requestContext);
		return session == null ? "<no user connected>" : session.getUserName();
	}

	@Override
	public Collection<String> correspondingPermissions(ContainerRequestContext requestContext) {
		WebSessionPermission session = currentSessionInformation(requestContext);
		return session == null ? ImmutableList.of() : session.getPermissions();
	}

	@SuppressWarnings("unchecked")
	public T currentSessionInformation(ContainerRequestContext request) {
		Object webSession = request.getProperty(REQUEST_SESSION_ATTRIBUTE_NAME);
		if(webSession == null) {
			String webSessionSerialized = parseAuthorizationBearer(request);
			webSession = webSessionSerialized == null ?
				null
				: webSessionSigner.parseSession(webSessionSerialized, (Class<WebSessionPermission>) webSessionClass);
			if(webSession == null) {
				webSession = EMPTY_SESSION;
			}
			request.setProperty(REQUEST_SESSION_ATTRIBUTE_NAME, webSession);
		}
		return webSession == EMPTY_SESSION ? null : (T) webSession;
	}

	private String parseAuthorizationBearer(ContainerRequestContext request) {
		String authorization = request.getHeaderString(HttpHeaders.AUTHORIZATION);
		if(authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}
		return authorization.substring(BEARER_PREFIX.length());
	}

}