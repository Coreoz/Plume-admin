package com.coreoz.plume.admin.webservices.security;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;

import com.coreoz.plume.jersey.security.WsRequestPermissionProvider;
import com.google.common.collect.ImmutableList;

@Singleton
public class AdminPermissionProvider implements WsRequestPermissionProvider {

	private final SessionRequestHandler sessionRequestHandler;

	@Inject
	public AdminPermissionProvider(SessionRequestHandler sessionRequestHandler) {
		this.sessionRequestHandler = sessionRequestHandler;
	}

	@Override
	public Collection<String> correspondingPermissions(ContainerRequestContext requestContext) {
		SessionInformation session = sessionRequestHandler.currentSessionInformation(requestContext);
		return session == null ? ImmutableList.of() : session.getPermissions();
	}

	@Override
	public String userInformation(ContainerRequestContext requestContext) {
		SessionInformation session = sessionRequestHandler.currentSessionInformation(requestContext);
		return session == null ? "No user connected" : session.getUsername();
	}

}
