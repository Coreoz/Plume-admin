package com.coreoz.plume.admin.websession;

import java.util.function.Function;

import javax.ws.rs.container.ContainerRequestContext;

public class WebSessionRequestFetcher<T extends WebSession> {

	private static final Object EMPTY_SESSION = new Object();

	private final WebSessionSigner<T> webSessionSigner;
	private final Function<ContainerRequestContext, String> webSessionFromRequest;
	private final String webSessionRequestAttribute;

	public WebSessionRequestFetcher(WebSessionSigner<T> webSessionSigner,
			Function<ContainerRequestContext, String> webSessionFromRequest,
			String webSessionRequestAttribute) {
		this.webSessionSigner = webSessionSigner;
		this.webSessionFromRequest = webSessionFromRequest;
		this.webSessionRequestAttribute = webSessionRequestAttribute;
	}

	@SuppressWarnings("unchecked")
	public T currentSessionInformation(ContainerRequestContext request) {
		Object webSession = request.getProperty(webSessionRequestAttribute);
		if(webSession == null) {
			String webSessionSerialized = webSessionFromRequest.apply(request);
			webSession = webSessionSerialized == null ? null : webSessionSigner.parseSession(webSessionSerialized);
			if(webSession == null) {
				webSession = EMPTY_SESSION;
			}
			request.setProperty(webSessionRequestAttribute, webSession);
		}
		return webSession == EMPTY_SESSION ? null : (T) webSession;
	}

}
