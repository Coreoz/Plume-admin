package com.coreoz.plume.admin.guice;

import com.coreoz.plume.admin.services.hash.BCryptHashService;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.permissions.AdminPermissionService;
import com.coreoz.plume.admin.services.permissions.AdminPermissionServiceBasic;
import com.coreoz.plume.admin.webservices.RoleWs;
import com.coreoz.plume.admin.webservices.SessionWs;
import com.coreoz.plume.admin.webservices.UsersWs;
import com.coreoz.plume.admin.webservices.security.AdminWsSecurityFeature;
import com.coreoz.plume.admin.webservices.security.WebSessionAdminProvider;
import com.coreoz.plume.admin.webservices.security.WebSessionProvider;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionSignerJwt;
import com.coreoz.plume.guice.GuiceServicesModule;
import com.google.inject.AbstractModule;

public class GuiceAdminModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GuiceServicesModule());

		bind(HashService.class).to(BCryptHashService.class);
		bind(WebSessionSigner.class).to(WebSessionSignerJwt.class);

		bind(AdminPermissionService.class).to(AdminPermissionServiceBasic.class);
		bind(WebSessionProvider.class).to(WebSessionAdminProvider.class);

		bind(RoleWs.class);
		bind(SessionWs.class);
		bind(UsersWs.class);
		bind(AdminWsSecurityFeature.class);
	}

}
