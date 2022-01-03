package com.coreoz.plume.admin.webservices;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.logs.AdminLoggerService;
import com.coreoz.plume.admin.services.logs.bean.LoggerLevel;
import com.coreoz.plume.admin.services.permission.SystemAdminPermissions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/admin/system/logs")
@Tag(name = "admin-loggers", description = "Manage application logger levels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(SystemAdminPermissions.MANAGE_SYSTEM)
@Singleton
public class LoggerWs {

    private final AdminLoggerService adminLoggerService;

    @Inject
    public LoggerWs(AdminLoggerService adminLoggerService) {
        this.adminLoggerService = adminLoggerService;
    }

    @GET
    @Operation(description = "Fetch current logger levels")
    public List<LoggerLevel> getLoggerLevels() {
        return adminLoggerService.getLoggerLevels();
    }

    @PUT
    @Path("/{level}/{name}")
    @Operation(description = "Change a logger level")
    public void changeLoggerLevel(@PathParam("name") String name, @PathParam("level") String level) {
        adminLoggerService.changeLoggerLevel(name, level);
    }
}
