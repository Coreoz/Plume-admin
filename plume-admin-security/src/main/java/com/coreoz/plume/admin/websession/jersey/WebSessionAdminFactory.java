package com.coreoz.plume.admin.websession.jersey;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.hk2.api.Factory;

import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.plume.admin.websession.WebSessionSigner;

/**
 * Jersey {@link Factory} to extract session from requests
 */
public class WebSessionAdminFactory implements Factory<WebSessionAdmin> {

	private final ContainerRequestContext context;
	private final WebSessionSigner webSessionSigner;

	@Inject
	public WebSessionAdminFactory(ContainerRequestContext context, WebSessionSigner sessionSigner) {
		this.context = context;
		this.webSessionSigner = sessionSigner;
	}

	@Override
	public WebSessionAdmin provide() {
		return JerseySessionParser.currentSessionInformationWithFingerprintCheck(context, webSessionSigner, WebSessionAdmin.class);
	}

	@Override
	public void dispose(WebSessionAdmin arg0) {
		// unused
	}

}
