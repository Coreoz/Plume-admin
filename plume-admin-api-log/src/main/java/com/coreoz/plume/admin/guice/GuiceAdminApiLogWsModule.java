package com.coreoz.plume.admin.guice;


import com.coreoz.plume.scheduler.guice.GuiceSchedulerModule;
import com.google.inject.AbstractModule;

public class GuiceAdminApiLogWsModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new GuiceSchedulerModule());
    }

}
