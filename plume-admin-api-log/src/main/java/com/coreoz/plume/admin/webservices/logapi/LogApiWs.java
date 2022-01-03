package com.coreoz.plume.admin.webservices.logapi;

import java.time.Instant;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.db.daos.LogApiTrimmed;
import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.configuration.LogApiConfigurationService;
import com.coreoz.plume.admin.services.logapi.LogApiBean;
import com.coreoz.plume.admin.services.logapi.LogApiFilters;
import com.coreoz.plume.admin.services.logapi.LogApiService;
import com.coreoz.plume.admin.services.permission.LogApiAdminPermissions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/admin")
@Tag(name = "admin-logs", description = "Application HTTP API trace")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(LogApiAdminPermissions.MANAGE_API_LOGS)
@Singleton
public class LogApiWs {
    private final LogApiService logApiService;
    private final Integer maxLogsToFetch;

    @Inject
    public LogApiWs(LogApiService logApiService, LogApiConfigurationService logApiConfigurationService) {
        this.logApiService = logApiService;
        this.maxLogsToFetch = logApiConfigurationService.defaultLimit();
    }

    @GET
    @Operation(description = "Fetch API trimmed logs (without request/response bodies) by query filters")
    @Path("/logs")
    public List<LogApiTrimmed> fetchAllLogs(
        @QueryParam("limit") Integer limit,
        @QueryParam("method") String method,
        @QueryParam("statusCode") Integer statusCode,
        @QueryParam("apiName") String apiName,
        @QueryParam("url") String url,
        @QueryParam("startDate") Instant startDate,
        @QueryParam("endDate") Instant endDate
    ) {
        if (limit == null || limit == 0 || limit > maxLogsToFetch) {
            limit = maxLogsToFetch;
        }
        return logApiService.fetchAllTrimmedLogs(limit, method, statusCode, apiName, url, startDate, endDate);
    }

    @GET
    @Operation(description = "Fetch the headers and the trimmed body of a request/response")
    @Path("/logs/{idLog}")
    public LogApiBean details(@PathParam("idLog") Long id) {
        return logApiService.fetchLogDetails(id);
    }

    @GET
    @Operation(description = "Download the body of a request or a response")
    @Path("/logs/{idLog}/{isRequest}")
    public Response bodyFile(@PathParam("idLog") Long id, @PathParam("isRequest") Boolean isRequest) {
        return logApiService
            .findBodyPart(id, isRequest != null && isRequest)
            .map(bodyPart -> Response
                .ok(bodyPart.getBody().getBytes())
                .header(
                    "Content-Disposition",
                    "attachment; filename=\"" + bodyPart.getApiName() + "." + bodyPart.getFileExtension() + "\""
                )
                .build()
            )
            .orElseGet(() -> Response.status(Status.NOT_FOUND).build());
    }

    @GET
    @Operation(description = "Fetch the headers and the trimmed body of a request/response")
    @Path("/logs-filters")
    public LogApiFilters filters() {
        return logApiService.fetchAvailableFilters();
    }

}
