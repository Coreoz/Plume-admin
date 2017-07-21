package com.coreoz.plume.admin.webservices.context;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.hk2.api.Factory;

import com.coreoz.plume.admin.webservices.security.WebSessionAdmin;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.admin.websession.jersey.WebSessionRequestPermissionProvider;

public class WebSessionAdminFactory implements Factory<WebSessionAdmin> {

	private final ContainerRequestContext context;
	private final WebSessionRequestPermissionProvider<WebSessionAdmin> webSessionRequestPermissionProvider;

	@Inject
	public WebSessionAdminFactory(ContainerRequestContext context, WebSessionSigner sessionSigner) {
		this.context = context;
		this.webSessionRequestPermissionProvider = new WebSessionRequestPermissionProvider<>(
			sessionSigner,
			WebSessionAdmin.class
		);
	}

	@Override
	public WebSessionAdmin provide() {
		return webSessionRequestPermissionProvider.currentSessionInformation(context);
	}

	@Override
	public void dispose(WebSessionAdmin arg0) {
		// unused
	}

}
