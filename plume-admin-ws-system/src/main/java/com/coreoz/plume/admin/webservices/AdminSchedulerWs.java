package com.coreoz.plume.admin.webservices;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.permission.SystemAdminPermissions;
import com.coreoz.plume.admin.services.scheduled.AdminSchedulerService;
import com.coreoz.plume.admin.services.scheduled.bean.AdminSchedulerData;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.jersey.errors.WsException;
import com.coreoz.wisp.Job;
import com.coreoz.wisp.Scheduler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/admin/system/scheduler")
@Tag(name = "admin-scheduler", description = "Manage scheduledJobs web-services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(SystemAdminPermissions.MANAGE_SYSTEM)
@Singleton
public class AdminSchedulerWs {

    private static final Logger logger = LoggerFactory.getLogger(AdminSchedulerWs.class);

    private final AdminSchedulerService adminSchedulerService;
    private final Scheduler scheduler;

    @Inject
    public AdminSchedulerWs(AdminSchedulerService adminSchedulerService, Scheduler scheduler) {
        this.adminSchedulerService = adminSchedulerService;
        this.scheduler = scheduler;
    }

    @GET
    @Operation(description = "Fetch scheduler jobs and thread pool information")
    public AdminSchedulerData getAdminSchedulerData() {
        return new AdminSchedulerData(
            adminSchedulerService.getSchedulerJobs(),
            adminSchedulerService.getSchedulerThreadStats());
    }

    @POST
    @Path("/{name}")
    @Operation(description = "Execute a job on the scheduler ASAP")
    public void executeJobNow(@PathParam("name") String name, @Context WebSessionPermission connectedUser) {
        Job jobToExecute = scheduler.findJob(name).orElseThrow(() -> new WsException(SystemWsError.TASK_NOT_FOUND));
        logger.info("Manual execution of {} by {}", name, connectedUser.getUserName());
        adminSchedulerService.executeJobNow(jobToExecute);
    }
}
