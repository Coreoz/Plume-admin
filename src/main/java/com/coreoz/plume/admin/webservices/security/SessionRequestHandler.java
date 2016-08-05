package com.coreoz.plume.admin.webservices.security;

import javax.ws.rs.container.ContainerRequestContext;

public interface SessionRequestHandler {

	/**
	 * Returns the user session for the current request if it exists and is valid or null otherwise
	 */
	SessionInformation currentSessionInformation(ContainerRequestContext request);

}
