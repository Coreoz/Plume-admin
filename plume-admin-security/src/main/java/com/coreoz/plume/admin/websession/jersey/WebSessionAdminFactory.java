package com.coreoz.plume.admin.websession.jersey;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.glassfish.hk2.api.Factory;

import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.plume.admin.websession.WebSessionSigner;

/**
 * Jersey {@link Factory} to extract session from requests
 */
public class WebSessionAdminFactory implements Factory<WebSessionAdmin> {

	private final ContainerRequestContext context;
	private final WebSessionSigner webSessionSigner;
	private final boolean verifyCookieFingerprint;

	@Inject
	public WebSessionAdminFactory(ContainerRequestContext context, WebSessionSigner sessionSigner,
			AdminSecurityConfigurationService configurationService) {
		this.context = context;
		this.webSessionSigner = sessionSigner;
		this.verifyCookieFingerprint = configurationService.sessionUseFingerprintCookie();
	}

	@Override
	public WebSessionAdmin provide() {
		return JerseySessionParser.currentSessionInformation(context, webSessionSigner, WebSessionAdmin.class, verifyCookieFingerprint);
	}

	@Override
	public void dispose(WebSessionAdmin arg0) {
		// unused
	}

}
