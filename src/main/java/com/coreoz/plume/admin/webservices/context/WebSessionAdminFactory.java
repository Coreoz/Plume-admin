package com.coreoz.plume.admin.webservices.context;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.hk2.api.Factory;

import com.coreoz.plume.admin.security.permission.AdminWebSessionWsSecurityFeature;
import com.coreoz.plume.admin.webservices.security.WebSessionAdmin;

public class WebSessionAdminFactory implements Factory<WebSessionAdmin> {

	private final ContainerRequestContext context;

	@Inject
	public WebSessionAdminFactory(ContainerRequestContext context) {
		this.context = context;
	}

	@Override
	public WebSessionAdmin provide() {
		// TODO to refactor
		return (WebSessionAdmin)
			context.getProperty(AdminWebSessionWsSecurityFeature.REQUEST_SESSION_ATTRIBUTE_NAME);
	}

	@Override
	public void dispose(WebSessionAdmin arg0) {
	}

}
