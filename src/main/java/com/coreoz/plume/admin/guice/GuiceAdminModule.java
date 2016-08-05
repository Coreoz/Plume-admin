package com.coreoz.plume.admin.guice;

import com.coreoz.plume.admin.services.hash.BCryptHashService;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.time.SystemTimeProvider;
import com.coreoz.plume.admin.services.time.TimeProvider;
import com.coreoz.plume.admin.webservices.security.AdminPermissionProvider;
import com.coreoz.plume.admin.webservices.security.SessionRequestHandler;
import com.coreoz.plume.admin.webservices.security.SessionRequestHandlerDefault;
import com.coreoz.plume.admin.webservices.security.SessionSigner;
import com.coreoz.plume.admin.webservices.security.SessionSignerJwt;
import com.coreoz.plume.jersey.security.WsRequestPermissionProvider;
import com.google.inject.AbstractModule;

public class GuiceAdminModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TimeProvider.class).to(SystemTimeProvider.class);
		bind(HashService.class).to(BCryptHashService.class);

		bind(SessionRequestHandler.class).to(SessionRequestHandlerDefault.class);
		bind(SessionSigner.class).to(SessionSignerJwt.class);
		bind(WsRequestPermissionProvider.class).to(AdminPermissionProvider.class);
	}

}
