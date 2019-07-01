package com.coreoz.plume.admin.webservices.logApi;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.coreoz.plume.admin.db.daos.LogApiTrimmed;
import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.logApi.LogApiBean;
import com.coreoz.plume.admin.services.logApi.LogApiService;
import com.coreoz.plume.admin.services.permission.ProjectAdminPermission;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/admin/logs")
@Api("Application HTTP API trace")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(ProjectAdminPermission.MANAGE_API_LOGS)
@Singleton
public class LogApiWs {
    private LogApiService logApiService;

    @Inject
    public LogApiWs(LogApiService logApiService) {
        this.logApiService = logApiService;
    }

    @GET
    @ApiOperation("Fetch all API trimmed logs (without request/response bodies)")
    public List<LogApiTrimmed> fetchAllLogs() {
        return logApiService.fetchAllTrimmedLogs();
    }

    @GET
    @ApiOperation("Download the body of a request or a response")
    @Path("/{idLog}")
    public LogApiBean details(@PathParam("idLog") Long id) {
    	return logApiService.fetchLogDetails(id);
    }

    @GET
    @ApiOperation("Download the body of a request or a response")
    @Path("/{idLog}/{isRequest}")
    public Response bodyFile(@PathParam("idLog") Long id, @PathParam("isRequest") Boolean isRequest) {
        return logApiService
        	.findBodyPart(id, isRequest)
        	.map(bodyPart -> Response
        		.ok(bodyPart.getBody().getBytes())
        		.header(
        			"Content-Disposition",
        			"attachment; filename=\""+ bodyPart.getApiName()+ "." + bodyPart.getFileExtension() + "\""
        		)
        		.build()
        	)
        	.orElseGet(() -> Response.status(Status.NOT_FOUND).build());
    }

}