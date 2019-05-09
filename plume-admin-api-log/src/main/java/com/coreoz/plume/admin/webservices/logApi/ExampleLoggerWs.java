package com.coreoz.plume.admin.webservices.logApi;

import com.coreoz.plume.admin.services.logApi.CountryBeanTest;
import com.coreoz.plume.admin.services.logApi.OneRefApiTestLogger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("/example")
@Api("Manage exemple web-services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ExampleLoggerWs {

    private final OneRefApiTestLogger oneRefApi;

    @Inject
    public ExampleLoggerWs(OneRefApiTestLogger oneRefApi) {
        this.oneRefApi = oneRefApi;
    }


    @GET
    @Path("test/retrofit")
    @ApiOperation("List countries")
    public List<CountryBeanTest> countries() throws IOException {
        return oneRefApi.countries();
    }

}

