package com.coreoz.plume.admin.websession.jersey;

import javax.ws.rs.container.ContainerRequestContext;

import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.google.common.net.HttpHeaders;

public class JerseyJwtSessionParser<T> {

	private static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";

	private static final String BEARER_PREFIX = "Bearer ";
	private static final Object EMPTY_SESSION = new Object();

	private final JwtSessionSigner<T> webSessionSigner;
	private final Class<T> webSessionClass;

	public JerseyJwtSessionParser(JwtSessionSigner<T> webSessionSigner, Class<T> webSessionClass) {
		this.webSessionSigner = webSessionSigner;
		this.webSessionClass = webSessionClass;
	}

	@SuppressWarnings("unchecked")
	public T currentSessionInformation(ContainerRequestContext request) {
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

	private String parseAuthorizationBearer(ContainerRequestContext request) {
		String authorization = request.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}
		return authorization.substring(BEARER_PREFIX.length());
	}

}
