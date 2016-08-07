package com.coreoz.plume.admin.security.permission;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.time.TimeProvider;
import com.coreoz.plume.admin.webservices.security.RestrictToAdmin;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AdminWebSessionWsSecurityFeature<T extends WebSessionPermission> implements DynamicFeature {

	private static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";
	private static final String HTTP_HEADER_TOKEN_NAME = "X-User-Token";

	private final WebSessionWsSecurityFeature<T, RestrictToAdmin> wsSecurityFeature;

	public AdminWebSessionWsSecurityFeature(AdminConfigurationService configurationService,
			ObjectMapper objectMapper, TimeProvider timeProvider,
			Class<T> webSessionAdminClass) {
		wsSecurityFeature = new WebSessionWsSecurityFeature<>(
			WebSessionWsSecurityFeatureConfiguration
				.<T, RestrictToAdmin> builder()
				.httpHeaderName(HTTP_HEADER_TOKEN_NAME)
				.requestAttributeName(REQUEST_SESSION_ATTRIBUTE_NAME)
				.jwtSecret(configurationService.jwtSecret())
				.permissionAnnotationExtractor(RestrictToAdmin::value)
				.permissionAnnotationType(RestrictToAdmin.class)
				.webSessionClass(webSessionAdminClass)
				.objectMapper(objectMapper)
				.timeProvider(timeProvider)
				.build()
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		wsSecurityFeature.configure(resourceInfo, context);
	}

}
