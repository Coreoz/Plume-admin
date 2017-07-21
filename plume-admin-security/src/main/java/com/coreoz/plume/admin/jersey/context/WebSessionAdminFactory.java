package com.coreoz.plume.admin.jersey.context;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.hk2.api.Factory;

import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.admin.websession.jersey.WebSessionRequestPermissionProvider;

public class WebSessionAdminFactory implements Factory<WebSessionPermission> {

	private final ContainerRequestContext context;
	private final WebSessionRequestPermissionProvider<WebSessionPermission> webSessionRequestPermissionProvider;

	@Inject
	public WebSessionAdminFactory(ContainerRequestContext context, WebSessionSigner sessionSigner) {
		this.context = context;
		this.webSessionRequestPermissionProvider = new WebSessionRequestPermissionProvider<>(
			sessionSigner,
			WebSessionPermission.class
		);
	}

	@Override
	public WebSessionPermission provide() {
		return webSessionRequestPermissionProvider.currentSessionInformation(context);
	}

	@Override
	public void dispose(WebSessionPermission arg0) {
		// unused
	}

}
