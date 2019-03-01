package com.coreoz.plume.admin.guice;


import com.coreoz.plume.admin.jersey.feature.WebSessionClassProvider;
import com.coreoz.plume.admin.services.permission.ProjectAdminPermissionService;
import com.coreoz.plume.admin.services.permissions.AdminPermissionService;
import com.coreoz.plume.admin.webservices.security.WebSessionAdminProvider;
import com.coreoz.plume.admin.webservices.security.WebSessionProvider;
import com.coreoz.plume.conf.guice.GuiceConfModule;
import com.coreoz.plume.db.querydsl.guice.GuiceQuerydslModule;
import com.coreoz.plume.jersey.guice.GuiceJacksonModule;
import com.google.inject.AbstractModule;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Group the Guice modules to install in the application
 */
public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GuiceConfModule());
		install(new GuiceJacksonModule());
		// database & Querydsl installation
		install(new GuiceQuerydslModule());

		//Plume permission
		install(new GuiceAdminWsModule());
		bind(WebSessionProvider.class).to(WebSessionAdminProvider.class);
		bind(WebSessionClassProvider.class).to(WebSessionAdminProvider.class);
		bind(AdminPermissionService.class).to(ProjectAdminPermissionService.class);
	}

}
