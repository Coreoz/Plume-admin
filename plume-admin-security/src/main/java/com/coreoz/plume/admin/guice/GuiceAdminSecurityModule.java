package com.coreoz.plume.admin.guice;

import com.coreoz.plume.admin.services.configuration.AdminTimingSecurityConfigurationService;
import com.coreoz.plume.guice.GuiceServicesModule;
import com.google.inject.AbstractModule;

public class GuiceAdminSecurityModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GuiceServicesModule());

		bind(AdminTimingSecurityConfigurationService.class);
	}

}
