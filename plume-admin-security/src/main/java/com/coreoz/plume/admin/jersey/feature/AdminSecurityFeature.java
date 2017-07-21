package com.coreoz.plume.admin.jersey.feature;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.admin.websession.jersey.WebSessionFeature;

@Singleton
public class AdminSecurityFeature implements DynamicFeature {

	private final WebSessionFeature<WebSessionPermission, RestrictToAdmin> webSessionSecurityFeature;

	@SuppressWarnings("unchecked")
	@Inject
	public AdminSecurityFeature(WebSessionSigner webSessionSigner,
			WebSessionClassProvider webSessionClassProvider) {
		this.webSessionSecurityFeature = new WebSessionFeature<>(
			webSessionSigner,
			(Class<WebSessionPermission>) webSessionClassProvider.webSessionClass(),
			RestrictToAdmin.class,
			RestrictToAdmin::value
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		webSessionSecurityFeature.configure(resourceInfo, context);
	}

}

