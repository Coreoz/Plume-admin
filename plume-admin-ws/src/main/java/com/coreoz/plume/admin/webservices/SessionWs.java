package com.coreoz.plume.admin.webservices;

import java.time.Clock;

import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.services.session.WebSessionAdminProvider;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.plume.jersey.security.permission.PublicApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * The default administration session web-service. All the logic is provided by
 * {@link AbstractSessionWs}; this class only wires the default {@link WebSessionAdmin} session type.
 */
@Path("/admin/session")
@Tag(name = "admin-session", description = "Manage the administration session")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
// This API is marked as public, since it must be accessed without any authentication
@PublicApi
@Singleton
public class SessionWs extends AbstractSessionWs<WebSessionAdmin> {

    @Inject
    public SessionWs(
        AdminUserService adminUserService,
        JwtSessionSigner jwtSessionSigner,
        AdminConfigurationService configurationService,
        AdminSecurityConfigurationService adminSecurityConfigurationService,
        Clock clock
    ) {
        super(
            adminUserService,
            jwtSessionSigner,
            configurationService,
            adminSecurityConfigurationService,
            clock,
            new WebSessionAdminProvider()
        );
    }

}
