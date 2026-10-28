package com.coreoz.plume.admin.webservices.session;

import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.session.AdminWebSessionConfig;
import com.coreoz.plume.admin.services.session.AuthSessionTokenHandler;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.plume.jersey.security.permission.PublicApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * The default administration session web-service: password login (inherited from
 * {@link PasswordSessionWs}) and token renewal.
 */
@Path("/admin/session")
@Tag(name = "admin-session", description = "Manage the administration session")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
// This API is marked as public, since it must be accessed without any authentication
@PublicApi
@Singleton
public class SessionWs extends PasswordSessionWs<WebSessionAdmin> {

    @Inject
    public SessionWs(
        AdminUserService adminUserService,
        AdminConfigurationService configurationService,
        AuthSessionTokenHandler authSessionTokenHandler
    ) {
        super(
            adminUserService,
            configurationService,
            authSessionTokenHandler,
            new AdminWebSessionConfig()
        );
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(description = "Renew a valid session token")
    public Response renew(String webSessionSerialized) {
        return renewSession(webSessionSerialized);
    }

}
