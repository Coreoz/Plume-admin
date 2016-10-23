package com.coreoz.plume.admin.webservices.security;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.jersey.WebSessionFeature;
import com.coreoz.plume.admin.websession.WebSessionSigner;

@Singleton
public class AdminSecurityFeature implements DynamicFeature {

	private final WebSessionFeature<WebSessionAdmin, RestrictToAdmin> webSessionSecurityFeature;

	@SuppressWarnings("unchecked")
	@Inject
	public AdminSecurityFeature(WebSessionSigner webSessionSigner,
			WebSessionProvider webSessionProvider) {
		this.webSessionSecurityFeature = new WebSessionFeature<>(
			webSessionSigner,
			(Class<WebSessionAdmin>) webSessionProvider.webSessionClass(),
			RestrictToAdmin.class,
			RestrictToAdmin::value
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		webSessionSecurityFeature.configure(resourceInfo, context);
	}

}

