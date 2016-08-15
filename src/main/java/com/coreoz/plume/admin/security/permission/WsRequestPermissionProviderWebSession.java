package com.coreoz.plume.admin.security.permission;

import java.util.Collection;

import javax.ws.rs.container.ContainerRequestContext;

import com.coreoz.plume.admin.websession.WebSessionRequestFetcher;
import com.coreoz.plume.jersey.security.WsRequestPermissionProvider;
import com.google.common.collect.ImmutableList;

public class WsRequestPermissionProviderWebSession implements WsRequestPermissionProvider {

	private final WebSessionRequestFetcher<? extends WebSessionPermission> webSessionRequestFetcher;

	WsRequestPermissionProviderWebSession(
		WebSessionRequestFetcher<? extends WebSessionPermission> webSessionRequestFetcher) {
		this.webSessionRequestFetcher = webSessionRequestFetcher;
	}

	@Override
	public String userInformation(ContainerRequestContext requestContext) {
		WebSessionPermission session = webSessionRequestFetcher.currentSessionInformation(requestContext);
		return session == null ? "No user connected" : session.getUserName();
	}

	@Override
	public Collection<String> correspondingPermissions(ContainerRequestContext requestContext) {
		WebSessionPermission session = webSessionRequestFetcher.currentSessionInformation(requestContext);
		return session == null ? ImmutableList.of() : session.getPermissions();
	}

}