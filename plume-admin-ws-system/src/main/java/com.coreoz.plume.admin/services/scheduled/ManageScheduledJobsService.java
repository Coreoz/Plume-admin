package com.coreoz.plume.admin.services.scheduled;

import com.coreoz.plume.admin.services.scheduled.bean.AsyncTask;
import com.coreoz.plume.admin.services.scheduled.bean.ThreadBean;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class ManageScheduledJobsService {

    private static final Logger logger = LoggerFactory.getLogger(ManageScheduledJobsService.class);

    public static Scheduler scheduler;

    @Inject
    public ManageScheduledJobsService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public List<AsyncTask> getAsyncTasks() {
        return scheduler.jobStatus()
            .stream()
            .map(job -> {
                long seconds = TimeUnit.MILLISECONDS.toSeconds(job.nextExecutionTimeInMillis()) - Instant.now().getEpochSecond();
                return new AsyncTask(
                    job.name(),
                    String.valueOf(job.schedule()),
                    job.executionsCount(),
                    job.nextExecutionTimeInMillis(),
                    job.lastExecutionTimeInMillis() != null ? job.lastExecutionTimeInMillis() : 0L,
                    String.valueOf(job.status()),
                    ((seconds > 5) && (job.status() == JobStatus.SCHEDULED)) || (job.status() == JobStatus.DONE)
                );
            })
            .collect(Collectors.toList());
    }

    public void executeAsyncTask(String name, Optional<Job> jobToExecute) {
        long secondsToNextExecution = (jobToExecute.get().nextExecutionTimeInMillis() - Instant.now().getEpochSecond()) / 1000;
        Schedule scheduleToExecute = jobToExecute.get().schedule();
        if ((secondsToNextExecution > 5) && (jobToExecute.get().status() == JobStatus.SCHEDULED)) {
            scheduler.cancel(name);
            scheduler.schedule(name, jobToExecute.get().runnable(), Schedules.afterInitialDelay(scheduleToExecute, Duration.ofSeconds(2)));
        } else if (jobToExecute.get().status() == JobStatus.DONE) {
            scheduler.schedule(name, jobToExecute.get().runnable(), Schedules.executeOnce(Schedule.willNeverBeExecuted));
        } else {
            logger.warn("The job {} will not be planned to be executed now since it is already executing or will be executed in less than 5 seconds", jobToExecute.get().name());
        }
    }

    public ThreadBean getThreadStat() {
        return new ThreadBean(
            scheduler.stats().getThreadPoolStats().getActiveThreads(),
            scheduler.stats().getThreadPoolStats().getIdleThreads()
        );
    }
}
