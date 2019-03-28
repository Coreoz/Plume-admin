package com.coreoz.plume.admin.webservices.api;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.permission.ProjectAdminPermission;
import com.coreoz.plume.admin.services.scheduled.ManageScheduledJobsService;
import com.coreoz.plume.admin.services.scheduled.bean.TasksAndThreadBean;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.jersey.errors.WsException;
import com.coreoz.wisp.Job;
import com.coreoz.wisp.Scheduler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/system/scheduler")
@Api("Manage scheduledJobs web-services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(ProjectAdminPermission.MANAGE_SYSTEM)
@Singleton
public class ManageScheduledWs {

    private static final Logger logger = LoggerFactory.getLogger(ManageScheduledWs.class);

    private final ManageScheduledJobsService manageScheduledJobsService;
    private final Scheduler scheduler;

    @Inject
    public ManageScheduledWs(ManageScheduledJobsService manageScheduledJobsService, Scheduler scheduler) {
        this.manageScheduledJobsService = manageScheduledJobsService;
        this.scheduler = scheduler;
    }

    @GET
    @ApiOperation("Fetch Async Task And Thread Task")
    public TasksAndThreadBean getTasksAndThread() {
        return new TasksAndThreadBean(
            manageScheduledJobsService.getAsyncTasks(),
            manageScheduledJobsService.getThreadStat());
    }

    @POST
    @Path("/{name}")
    @ApiOperation("Execute Async Task")
    public void executeScheduled(@PathParam("name") String name, @Context WebSessionPermission connectedUser) {
        Job jobToExecute = scheduler.findJob(name).orElseThrow(() -> new WsException(SystemWsError.TASK_NOT_FOUND));
        logger.info("Manual execution of {} by {}", name, connectedUser.getUserName());
        manageScheduledJobsService.executeAsyncTask(name, jobToExecute);
    }
}
