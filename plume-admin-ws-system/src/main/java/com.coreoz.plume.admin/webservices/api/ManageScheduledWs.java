package com.coreoz.plume.admin.webservices.api;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.permission.ProjectAdminPermission;
import com.coreoz.plume.admin.services.scheduled.ManageScheduledJobsService;
import com.coreoz.plume.admin.services.scheduled.bean.TasksAndThreadBean;
import com.coreoz.plume.jersey.errors.WsException;
import com.coreoz.wisp.Scheduler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/system/scheduler")
@Api("Manage scheduledJobs web-services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(ProjectAdminPermission.MANAGE_SYSTEM)
@Singleton
public class ManageScheduledWs {

    private final ManageScheduledJobsService manageScheduledJobsService;

    @Inject
    public ManageScheduledWs(ManageScheduledJobsService manageScheduledJobsService) {
        this.manageScheduledJobsService = manageScheduledJobsService;
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
    public void executeScheduled(@PathParam("name") String name) {
        Scheduler scheduler = new Scheduler();
        if(!scheduler.findJob(name).isPresent())
        {
            throw new WsException(SystemWsError.TASK_NOT_FOUND);
        } else {
            manageScheduledJobsService.executeAsyncTask(name, scheduler.findJob(name));
        }
    }
}
