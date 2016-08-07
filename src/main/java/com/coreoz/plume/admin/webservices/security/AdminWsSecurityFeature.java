package com.coreoz.plume.admin.webservices.security;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.security.permission.AdminWebSessionWsSecurityFeature;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.time.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class AdminWsSecurityFeature implements DynamicFeature {

	private final AdminWebSessionWsSecurityFeature<WebSessionAdmin> wsSecurityFeature;

	@Inject
	public AdminWsSecurityFeature(AdminConfigurationService configurationService,
			ObjectMapper objectMapper, TimeProvider timeProvider) {
		wsSecurityFeature = new AdminWebSessionWsSecurityFeature<>(
			configurationService,
			objectMapper,
			timeProvider,
			WebSessionAdmin.class
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		wsSecurityFeature.configure(resourceInfo, context);
	}

}
