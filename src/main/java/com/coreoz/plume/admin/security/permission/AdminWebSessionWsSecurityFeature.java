package com.coreoz.plume.admin.security.permission;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.webservices.security.RestrictToAdmin;
import com.coreoz.plume.admin.websession.WebSessionSigner;

public class AdminWebSessionWsSecurityFeature implements DynamicFeature {

	private static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";

	private final WebSessionWsSecurityFeature<RestrictToAdmin> wsSecurityFeature;

	public AdminWebSessionWsSecurityFeature(WebSessionSigner webSessionSigner,
			Class<? extends WebSessionPermission> webSessionAdminClass) {
		wsSecurityFeature = new WebSessionWsSecurityFeature<>(
			WebSessionWsSecurityFeatureConfiguration
				.<RestrictToAdmin> builder()
				.requestAttributeName(REQUEST_SESSION_ATTRIBUTE_NAME)
				.permissionAnnotationExtractor(RestrictToAdmin::value)
				.permissionAnnotationType(RestrictToAdmin.class)
				.webSessionClass(webSessionAdminClass)
				.webSessionSigner(webSessionSigner)
				.build()
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		wsSecurityFeature.configure(resourceInfo, context);
	}

}
