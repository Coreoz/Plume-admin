package com.coreoz.plume.admin.services.scheduler;


import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.services.configuration.LogApiConfigurationService;
import com.coreoz.plume.admin.services.logApi.LogApiService;
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

    public void startScheduler() {
        scheduler.schedule(
            "Delete logs older than X days",
            logApiService::deleteOldLogs,
            Schedules.executeAt(configurationService.getLogTiming())
        );
        scheduler.schedule(
            "Delete logs for each url if the number of logs by is greater than X ",
            logApiService::cleanLogsNumberByApiName,
            Schedules.fixedDelaySchedule(Duration.ofMinutes(configurationService.getLogCleanDelay()))
        );
    }

}
