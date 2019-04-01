package com.coreoz.plume.admin.webservices.api;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.permission.ProjectAdminPermission;
import com.coreoz.plume.admin.services.logs.LoggerService;
import com.coreoz.plume.admin.services.logs.bean.LoggerLevel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/system/logs")
@Api("Manage logs web-services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(ProjectAdminPermission.MANAGE_SYSTEM)
@Singleton
public class LoggerWs {

    private final LoggerService loggerService;

    @Inject
    public LoggerWs(LoggerService loggerService) {
        this.loggerService = loggerService;
    }

    @GET
    @ApiOperation("Fetch Loggers")
    public List<LoggerLevel> getLogList() {
        return loggerService.getLogList();
    }

    @PUT
    @Path("/{level}/{name}")
    @ApiOperation("Update Logger")
    public void updateLog(@PathParam("name") String name, @PathParam("level") String level) {
        loggerService.updateLog(name, level);
    }
}
