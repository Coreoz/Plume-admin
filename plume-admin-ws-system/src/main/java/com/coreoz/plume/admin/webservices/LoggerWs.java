package com.coreoz.plume.admin.webservices;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.logs.AdminLoggerService;
import com.coreoz.plume.admin.services.logs.bean.LoggerLevel;
import com.coreoz.plume.admin.services.permission.SystemAdminPermissions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
