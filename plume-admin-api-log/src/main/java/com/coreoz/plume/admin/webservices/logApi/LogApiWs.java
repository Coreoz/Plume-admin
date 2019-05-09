package com.coreoz.plume.admin.webservices.logApi;


import com.coreoz.plume.admin.db.generated.LogApi;
import com.coreoz.plume.admin.services.logApi.LogApiBean;
import com.coreoz.plume.admin.services.logApi.LogApiService;
import com.coreoz.plume.admin.services.logApi.LogHeaderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/admin/logs")
@Api("Manage api logs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class LogApiWs {
    private LogApiService logApiService;
    private LogHeaderService logHeaderService;

    @Inject
    public LogApiWs(LogApiService logApiService, LogHeaderService logHeaderService) {
        this.logApiService = logApiService;
        this.logHeaderService = logHeaderService;
    }


    @GET
    @ApiOperation("get all logs ")
    public List<LogApiBean> getAllLogs() {
        return logApiService.getAllLogs();
    }

    @GET
    @Path("/{idLog}/{isRequest}")
    @Produces("text/plain")
    public Response getTextFile(@PathParam("idLog") Long id, @PathParam("isRequest") Boolean isRequest) {
        LogApi log = logApiService.findById(id);
        LogApiBean logNotFullText = logApiService.getLogbyId(id);
        Response.ResponseBuilder response;
        String mode;
        if (isRequest){
            response = Response.ok(log.getBodyRequest().getBytes());
            mode = logHeaderService.getMode(logNotFullText.getHeaderRequest());
        }
        else{
            response = Response.ok(log.getBodyResponse().getBytes());
            mode = logHeaderService.getMode(logNotFullText.getHeaderResponse());
        }
        response.header("Content-Disposition", "attachment; filename=\""+ log.getApi()+ "." + mode + "\"");
        return response.build();

    }
}
