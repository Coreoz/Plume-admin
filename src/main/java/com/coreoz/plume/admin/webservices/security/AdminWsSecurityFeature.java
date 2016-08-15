package com.coreoz.plume.admin.webservices.security;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.security.permission.AdminWebSessionWsSecurityFeature;
import com.coreoz.plume.admin.websession.WebSessionSigner;

@Singleton
public class AdminWsSecurityFeature implements DynamicFeature {

	private final AdminWebSessionWsSecurityFeature wsSecurityFeature;

	@Inject
	public AdminWsSecurityFeature(WebSessionSigner webSessionSigner,
			WebSessionProvider webSessionProvider) {
		wsSecurityFeature = new AdminWebSessionWsSecurityFeature(
			webSessionSigner,
			webSessionProvider.webSessionClass()
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		wsSecurityFeature.configure(resourceInfo, context);
	}

}
