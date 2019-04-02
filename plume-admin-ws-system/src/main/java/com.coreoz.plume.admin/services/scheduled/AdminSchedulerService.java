package com.coreoz.plume.admin.services.scheduled;

import com.coreoz.plume.admin.services.scheduled.bean.AdminSchedulerJob;
import com.coreoz.plume.admin.services.scheduled.bean.AdminSchedulerThreadStats;
import com.coreoz.wisp.Job;
import com.coreoz.wisp.JobStatus;
import com.coreoz.wisp.Scheduler;
import com.coreoz.wisp.schedule.Schedule;
import com.coreoz.wisp.schedule.Schedules;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class AdminSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AdminSchedulerService.class);

    private final Scheduler scheduler;

    private static final long JOB_NEXT_EXECUTION_MIN_DURATION_TO_ALLOW_EXECUTION_IN_MILLIS = 5000L;

    @Inject
    public AdminSchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public List<AdminSchedulerJob> getSchedulerJobs() {
        return scheduler.jobStatus()
            .stream()
            .map(job -> {
                long nextExecutionDurationInMillis = job.nextExecutionTimeInMillis() - System.currentTimeMillis();
                return new AdminSchedulerJob(
                    job.name(),
                    String.valueOf(job.schedule()),
                    job.executionsCount(),
                    job.nextExecutionTimeInMillis(),
                    job.lastExecutionStartedTimeInMillis() != null ? job.lastExecutionStartedTimeInMillis() : 0L,
                    job.lastExecutionEndedTimeInMillis() != null ? job.lastExecutionEndedTimeInMillis() : 0L,
                    String.valueOf(job.status()),
                    ((nextExecutionDurationInMillis > JOB_NEXT_EXECUTION_MIN_DURATION_TO_ALLOW_EXECUTION_IN_MILLIS) && (job.status() == JobStatus.SCHEDULED)) || (job.status() == JobStatus.DONE)
                );
            })
            .collect(Collectors.toList());
    }

    public synchronized void executeJobNow(Job jobToExecute) {
        String name = jobToExecute.name();
        long nextExecutionDurationInMillis = jobToExecute.nextExecutionTimeInMillis() - System.currentTimeMillis();
        Schedule scheduleToExecute = jobToExecute.schedule();
        if ((nextExecutionDurationInMillis > JOB_NEXT_EXECUTION_MIN_DURATION_TO_ALLOW_EXECUTION_IN_MILLIS) && (jobToExecute.status() == JobStatus.SCHEDULED)) {
            logger.debug("Cancelling the job {} to execute it now", name);
            scheduler.cancel(name);
            scheduler.schedule(name, jobToExecute.runnable(), Schedules.afterInitialDelay(scheduleToExecute, Duration.ZERO));
        } else if (jobToExecute.status() == JobStatus.DONE) {
            logger.debug("Executing once the job {} that has been cancelled", name);
            scheduler.schedule(name, jobToExecute.runnable(), Schedules.executeOnce(Schedule.willNeverBeExecuted));
        } else {
            logger.warn("The job {} will not be planned to be executed now since it is already executing or will be executed in less than 5 seconds", jobToExecute.name());
        }
    }

    public AdminSchedulerThreadStats getSchedulerThreadStats() {
        return new AdminSchedulerThreadStats(
            scheduler.stats().getThreadPoolStats().getActiveThreads(),
            scheduler.stats().getThreadPoolStats().getIdleThreads(),
            scheduler.stats().getThreadPoolStats().getMinThreads(),
            scheduler.stats().getThreadPoolStats().getMaxThreads(),
            scheduler.stats().getThreadPoolStats().getLargestPoolSize()
        );
    }
}
