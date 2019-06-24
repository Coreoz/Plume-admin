package com.coreoz.plume.admin.webservices;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.logs.AdminLoggerService;
import com.coreoz.plume.admin.services.logs.bean.LoggerLevel;
import com.coreoz.plume.admin.services.permission.ProjectAdminPermission;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/admin/system/logs")
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
