package com.coreoz.plume.admin.webservices.api;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.permission.ProjectAdminPermission;
import com.coreoz.plume.admin.services.logs.AdminLoggerService;
import com.coreoz.plume.admin.services.logs.bean.LoggerLevel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/system/logs")
@Api("Manage application logger levels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(ProjectAdminPermission.MANAGE_SYSTEM)
@Singleton
public class LoggerWs {

    private final AdminLoggerService adminLoggerService;

    @Inject
    public LoggerWs(AdminLoggerService adminLoggerService) {
        this.adminLoggerService = adminLoggerService;
    }

    @GET
    @ApiOperation("Fetch current logger levels")
    public List<LoggerLevel> getLoggerLevels() {
        return adminLoggerService.getLoggerLevels();
    }

    @PUT
    @Path("/{level}/{name}")
    @ApiOperation("Change a logger level")
    public void changeLoggerLevel(@PathParam("name") String name, @PathParam("level") String level) {
        adminLoggerService.changeLoggerLevel(name, level);
    }
}
