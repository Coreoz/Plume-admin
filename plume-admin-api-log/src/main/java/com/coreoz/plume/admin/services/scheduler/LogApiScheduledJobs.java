package com.coreoz.plume.admin.services.scheduler;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.coreoz.plume.admin.services.configuration.LogApiConfigurationService;
import com.coreoz.plume.admin.services.logapi.LogApiService;
import com.coreoz.wisp.Scheduler;
import com.coreoz.wisp.schedule.Schedules;

@Singleton
public class LogApiScheduledJobs {

    private final Scheduler scheduler;
    private final LogApiConfigurationService configurationService;
    private final LogApiService logApiService;

    @Inject
    public LogApiScheduledJobs(Scheduler scheduler,LogApiConfigurationService configurationService, LogApiService logApiService) {
        this.scheduler = scheduler;
        this.configurationService = configurationService;
        this.logApiService = logApiService;
    }

    public void scheduleJobs() {
        scheduler.schedule(
            "Clean API logs",
            logApiService::cleanUp,
            Schedules.fixedDelaySchedule(configurationService.cleaningRunningEvery())
        );
    }

}
