package com.coreoz.plume.admin.guice;

import com.coreoz.plume.admin.services.permissions.AdminPermissionService;
import com.coreoz.plume.admin.services.permissions.AdminPermissionServiceBasic;
import com.coreoz.plume.admin.webservices.security.WebSessionAdminProvider;
import com.coreoz.plume.admin.webservices.security.WebSessionProvider;
import com.google.inject.AbstractModule;

public class GuiceAdminWsWithDefaultsModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GuiceAdminWsModule());

		bind(AdminPermissionService.class).to(AdminPermissionServiceBasic.class);
		bind(WebSessionProvider.class).to(WebSessionAdminProvider.class);
	}

}