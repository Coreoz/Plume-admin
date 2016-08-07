package com.coreoz.plume.admin.guice;

import com.coreoz.plume.admin.services.hash.BCryptHashService;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.time.SystemTimeProvider;
import com.coreoz.plume.admin.services.time.TimeProvider;
import com.google.inject.AbstractModule;

public class GuiceAdminModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TimeProvider.class).to(SystemTimeProvider.class);
		bind(HashService.class).to(BCryptHashService.class);
	}

}
