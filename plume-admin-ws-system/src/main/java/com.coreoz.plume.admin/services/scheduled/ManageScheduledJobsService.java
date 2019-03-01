package com.coreoz.plume.admin.services.scheduled;

import com.coreoz.plume.admin.services.scheduled.bean.AsyncTaskBean;
import com.coreoz.plume.admin.services.scheduled.bean.TasksAndThreadBean;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class ManageScheduledJobsService {

    private static final Logger logger = LoggerFactory.getLogger(ManageScheduledJobsService.class);

    public static Scheduler scheduler;

    @Inject
    public ManageScheduledJobsService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public List<AsyncTaskBean> getAsyncTaskList() {
        List<AsyncTaskBean> asyncList = new ArrayList<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern(("dd/MM/yyyy HH:mm:ss"));

        // For each job ( = Async task) in scheduler, initialize an AsyncBean
        for (Job job : scheduler.jobStatus()) {
            AsyncTaskBean async = new AsyncTaskBean();

            async.setName(job.name());
            async.setNbExecution(job.executionsCount());
            async.setFrequency(String.valueOf(job.schedule()));
            async.setStatus(String.valueOf(job.status()));
            //For next and previous executions, convert millis to a LocalDateTime formatted "dd/MM/yyyy HH:mm:ss"
            async.setNextExecution((LocalDateTime.ofInstant(Instant.ofEpochMilli(job.nextExecutionTimeInMillis()), ZoneId.systemDefault())).format(format));
            if (job.lastExecutionTimeInMillis() != null) {
                async.setPreviousExecution((LocalDateTime.ofInstant(Instant.ofEpochMilli(job.lastExecutionTimeInMillis()), ZoneId.systemDefault())).format(format));
            } else {
                async.setPreviousExecution(LocalDateTime.now().format(format)); //Initialize with actual datetime if never executed yet
            }

            //Calculating number of seconds between now and next execution of the job
            long seconds = TimeUnit.MILLISECONDS.toSeconds(job.nextExecutionTimeInMillis()) - Instant.now().getEpochSecond();
            if (((seconds > 5) && (job.status() == JobStatus.SCHEDULED)) || (job.status() == JobStatus.DONE)) {
                async.setCanBeRun(true);
            } else {
                async.setCanBeRun(false);
            }

            //Add AsyncBean to the list
            asyncList.add(async);
        }

        return asyncList;
    }

    public void executeAsyncTask(String name) {

        //Because job is optional, check presence before executing it
        if (scheduler.findJob(name).isPresent()) {
            //Calculating number of seconds between now and next execution of the job
            long seconds = TimeUnit.MILLISECONDS.toSeconds(scheduler.findJob(name).get().nextExecutionTimeInMillis()) - Instant.now().getEpochSecond();
            Job jobToExecute = scheduler.findJob(name).get();
            Schedule scheduleToExecute = jobToExecute.schedule();

            if ((seconds > 5) && (jobToExecute.status() == JobStatus.SCHEDULED)) {
                scheduler.cancel(name);
                scheduler.schedule(name, jobToExecute.runnable(), Schedules.afterInitialDelay(scheduleToExecute, Duration.ofSeconds(2)));
            } else if (jobToExecute.status() == JobStatus.DONE) {
                scheduler.schedule(name, jobToExecute.runnable(), Schedules.executeOnce(scheduleToExecute));
            } else {
                logger.info("The job is executing / will execute in less than 5 seconds");
            }
        } else {
            logger.info("Can't find job with this name");
        }
    }

    public ThreadBean getThreadStat() {

        ThreadBean threads = new ThreadBean();

        threads.setActiveThreads(scheduler.stats().getThreadPoolStats().getActiveThreads());
        threads.setInactiveThreads(scheduler.stats().getThreadPoolStats().getIdleThreads());

        return threads;
    }

    public TasksAndThreadBean getTasksAndThread() {
        TasksAndThreadBean group = new TasksAndThreadBean();

        group.setAsyncTaskList(getAsyncTaskList());
        group.setThreadStats(getThreadStat());

        return group;
    }


}
