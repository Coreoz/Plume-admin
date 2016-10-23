package com.coreoz.plume.admin.webservices.context;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.hk2.api.Factory;

import com.coreoz.plume.admin.jersey.WebSessionFeature;
import com.coreoz.plume.admin.webservices.security.WebSessionAdmin;

public class WebSessionAdminFactory implements Factory<WebSessionAdmin> {

	private final ContainerRequestContext context;

	@Inject
	public WebSessionAdminFactory(ContainerRequestContext context) {
		this.context = context;
	}

	@Override
	public WebSessionAdmin provide() {
		return (WebSessionAdmin)
			context.getProperty(WebSessionFeature.REQUEST_SESSION_ATTRIBUTE_NAME);
	}

	@Override
	public void dispose(WebSessionAdmin arg0) {
	}

}
