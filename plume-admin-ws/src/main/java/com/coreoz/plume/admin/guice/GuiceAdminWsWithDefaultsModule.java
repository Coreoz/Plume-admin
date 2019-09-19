package com.coreoz.plume.admin.guice;

import com.coreoz.plume.admin.services.permissions.AdminPermissionService;
import com.coreoz.plume.admin.services.permissions.AdminPermissionServiceBasic;
import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.coreoz.plume.admin.websession.JwtSessionSignerProvider;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.google.inject.AbstractModule;

public class GuiceAdminWsWithDefaultsModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GuiceAdminWsModule());

		bind(AdminPermissionService.class).to(AdminPermissionServiceBasic.class);
		bind(WebSessionSigner.class).toProvider(JwtSessionSignerProvider.class);
		bind(JwtSessionSigner.class).toProvider(JwtSessionSignerProvider.class);
	}

}