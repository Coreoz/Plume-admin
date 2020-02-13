package com.coreoz.plume.admin.jersey.feature;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.admin.websession.jersey.WebSessionRequestPermissionProvider;
import com.coreoz.plume.jersey.security.permission.PermissionFeature;

/**
 * The Jersey feature that will secure a resource (JAX-RS Java class) annotated with {@link RestrictToAdmin}
 */
@Singleton
public class AdminSecurityFeature implements DynamicFeature {

	private final PermissionFeature<RestrictToAdmin> permissionFeature;

	@Inject
	public AdminSecurityFeature(WebSessionSigner webSessionSigner, AdminSecurityConfigurationService configurationService) {
		this.permissionFeature = new PermissionFeature<>(
			new WebSessionRequestPermissionProvider<>(
				webSessionSigner,
				WebSessionAdmin.class,
				configurationService.sessionUseFingerprintCookie()
			),
			RestrictToAdmin.class,
			RestrictToAdmin::value
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		permissionFeature.configure(resourceInfo, context);
	}

}

