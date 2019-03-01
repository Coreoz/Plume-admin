package com.coreoz.plume.admin.webservices.api;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.permission.ProjectAdminPermission;
import com.coreoz.plume.admin.services.logs.ManageLogsJobService;
import com.coreoz.plume.admin.services.logs.bean.LogBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/system")
@Api("Manage logs web-services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(ProjectAdminPermission.MANAGE_SYSTEM)
@Singleton
public class ManageLogsWs {

    private final ManageLogsJobService manageLogsJobService;

    @Inject
    public ManageLogsWs(ManageLogsJobService manageLogsJobService) {
        this.manageLogsJobService = manageLogsJobService;
    }

    @GET
    @Path("/logs")
    @ApiOperation("Fetch Loggers")
    public List<LogBean> getLogList() {
        return manageLogsJobService.getLogList();
    }

    @POST
    @Path("/logs/update/{level}/{key}")
    @ApiOperation("Fetch Loggers")
    public void updateLog(@PathParam("key") Integer key, @PathParam("level") String level) {
        manageLogsJobService.updateLog(key, level);
    }

    @POST
    @Path("/logs/add/{level}/{name}")
    @ApiOperation("Fetch Loggers")
    public void addLog(@PathParam("name") String name, @PathParam("level") String level) {
        manageLogsJobService.addLog(name, level);
    }

}
