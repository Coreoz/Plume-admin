package com.coreoz.plume.admin.security.permission;

import java.util.Collection;

import javax.ws.rs.container.ContainerRequestContext;

import com.coreoz.plume.admin.websession.WebSessionRequestFetcher;
import com.coreoz.plume.jersey.security.WsRequestPermissionProvider;
import com.google.common.collect.ImmutableList;

public class WsRequestPermissionProviderWebSession<T extends WebSessionPermission> implements WsRequestPermissionProvider {

	private final WebSessionRequestFetcher<T> webSessionRequestFetcher;

	WsRequestPermissionProviderWebSession(WebSessionRequestFetcher<T> webSessionRequestFetcher) {
		this.webSessionRequestFetcher = webSessionRequestFetcher;
	}

	@Override
	public String userInformation(ContainerRequestContext requestContext) {
		T session = webSessionRequestFetcher.currentSessionInformation(requestContext);
		return session == null ? "No user connected" : session.getUsername();
	}

	@Override
	public Collection<String> correspondingPermissions(ContainerRequestContext requestContext) {
		T session = webSessionRequestFetcher.currentSessionInformation(requestContext);
		return session == null ? ImmutableList.of() : session.getPermissions();
	}

}