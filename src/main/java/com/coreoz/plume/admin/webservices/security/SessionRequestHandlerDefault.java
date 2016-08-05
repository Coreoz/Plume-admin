package com.coreoz.plume.admin.webservices.security;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;

@Singleton
public class SessionRequestHandlerDefault implements SessionRequestHandler {

	private static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";
	private static final String HTTP_HEADER_TOKEN_NAME = "X-User-Token";

	private static final SessionBasicInformation EMPTY_SESSION = new SessionBasicInformation();

	private final SessionSigner sessionSigner;

	@Inject
	public SessionRequestHandlerDefault(SessionSigner jwtTokens) {
		this.sessionSigner = jwtTokens;
	}

	@Override
	public SessionInformation currentSessionInformation(ContainerRequestContext request) {
		SessionBasicInformation session = (SessionBasicInformation) request.getProperty(REQUEST_SESSION_ATTRIBUTE_NAME);
		if(session == null) {
			String jwtToken = request.getHeaderString(HTTP_HEADER_TOKEN_NAME);
			session = jwtToken == null ? null : sessionSigner.parseSession(jwtToken, SessionBasicInformation.class);
			if(session == null) {
				session = EMPTY_SESSION;
			}
			request.setProperty(REQUEST_SESSION_ATTRIBUTE_NAME, session);
		}
		return session == EMPTY_SESSION ? null : session;
	}

}
