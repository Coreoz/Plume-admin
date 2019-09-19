package com.coreoz.plume.admin.websession.jersey;

import javax.ws.rs.container.ContainerRequestContext;

import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.google.common.net.HttpHeaders;

/**
 * Parse session from a {@link ContainerRequestContext} request
 */
public class JerseySessionParser {

	private static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";

	private static final String BEARER_PREFIX = "Bearer ";
	private static final Object EMPTY_SESSION = new Object();

	@SuppressWarnings("unchecked")
	public static <T> T currentSessionInformation(ContainerRequestContext request, WebSessionSigner webSessionSigner, Class<T> webSessionClass) {
		Object webSession = request.getProperty(REQUEST_SESSION_ATTRIBUTE_NAME);
		if (webSession == null) {
			String webSessionSerialized = parseAuthorizationBearer(request);
			webSession = webSessionSerialized == null ? null
					: webSessionSigner.parseSession(webSessionSerialized, webSessionClass);
			if (webSession == null) {
				webSession = EMPTY_SESSION;
			}
			request.setProperty(REQUEST_SESSION_ATTRIBUTE_NAME, webSession);
		}
		return webSession == EMPTY_SESSION ? null : (T) webSession;
	}

	private static String parseAuthorizationBearer(ContainerRequestContext request) {
		String authorization = request.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}
		return authorization.substring(BEARER_PREFIX.length());
	}

}
