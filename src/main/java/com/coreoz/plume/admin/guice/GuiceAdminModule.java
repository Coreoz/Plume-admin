package com.coreoz.plume.admin.guice;

import com.coreoz.plume.admin.services.hash.BCryptHashService;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.webservices.RoleWs;
import com.coreoz.plume.admin.webservices.SessionWs;
import com.coreoz.plume.admin.webservices.UsersWs;
import com.coreoz.plume.guice.GuiceServicesModule;
import com.google.inject.AbstractModule;

public class GuiceAdminModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GuiceServicesModule());

		bind(HashService.class).to(BCryptHashService.class);

		bind(RoleWs.class);
		bind(SessionWs.class);
		bind(UsersWs.class);
	}

}
