package com.coreoz.plume.admin.webservices.api;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.permission.ProjectAdminPermission;
import com.coreoz.plume.admin.services.scheduled.ManageScheduledJobsService;
import com.coreoz.plume.admin.services.scheduled.bean.TasksAndThreadBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/system")
@Api("Manage scheduledJobs web-services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(ProjectAdminPermission.MANAGE_SYSTEM)
@Singleton
public class ManageScheduledWs {

    private final ManageScheduledJobsService manageScheduledJobsService;

    @Inject
    public ManageScheduledWs(ManageScheduledJobsService manageScheduledJobsService) {this.manageScheduledJobsService = manageScheduledJobsService;}

    @GET
    @Path("/tasksAndThread")
    @ApiOperation("Fetch Async Task And Thread Task")
    public TasksAndThreadBean getTasksAndThread() { return manageScheduledJobsService.getTasksAndThread(); }

    @POST
    @Path("/task/{name}")
    @ApiOperation("Execute Async Task")
    public void executeScheduled(@PathParam("name") String name) {
        manageScheduledJobsService.executeAsyncTask(name);
    }
}
