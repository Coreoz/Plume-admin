package com.coreoz.plume.admin.webservices;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.guice.TestModule;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.services.session.WebSessionProvider;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.webservices.data.session.AdminCredentials;
import com.coreoz.plume.admin.webservices.data.session.AdminSession;
import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.test.GuiceTest;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.inject.Inject;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@GuiceTest(TestModule.class)
class AbstractSessionWsTest {
    @Inject
    private AdminUserService adminUserService;
    @Inject
    private AdminUserDao adminUserDao;
    @Inject
    private AdminRoleService adminRoleService;
    @Inject
    private HashService hashService;
    @Inject
    private JwtSessionSigner jwtSessionSigner;
    @Inject
    private AdminConfigurationService adminConfigurationService;
    @Inject
    private AdminSecurityConfigurationService adminSecurityConfigurationService;
    @Inject
    private Clock clock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_should_create_a_custom_session() {
        // Given
        String password = "password";
        AdminUser adminUser = mockAdminUser(password);
        AdminCredentials credentials = new AdminCredentials();
        credentials.setUserName(adminUser.getUserName());
        credentials.setPassword(password);
        AsyncResponse asyncResponse = mock(AsyncResponse.class);
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        // When
        when(adminUserDao.findByUserName(adminUser.getUserName())).thenReturn(Optional.of(adminUser));
        when(adminRoleService.findRolePermissions(1L)).thenReturn(List.of("PERMISSION"));
        customSessionWs().authenticate(asyncResponse, credentials);

        // Then
        verify(asyncResponse).resume(responseCaptor.capture());
        AdminSession session = (AdminSession) responseCaptor.getValue().getEntity();
        CustomSession customSession = jwtSessionSigner.parseSession(session.getWebSessionToken(), CustomSession.class);

        assertThat(customSession.getUserId()).isEqualTo(1L);
        assertThat(customSession.getUserName()).isEqualTo("testUser");
        assertThat(customSession.getPermissions()).containsExactly("PERMISSION");
    }

    @Test
    void renew_should_keep_custom_session_fingerprint_and_update_permissions() {
        // Given
        String password = "password";
        AdminUser adminUser = mockAdminUser(password);
        AdminCredentials credentials = new AdminCredentials();
        credentials.setUserName(adminUser.getUserName());
        credentials.setPassword(password);
        AsyncResponse asyncResponse = mock(AsyncResponse.class);
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        // When (initial authentication)
        when(adminUserDao.findByUserName(adminUser.getUserName())).thenReturn(Optional.of(adminUser));
        when(adminRoleService.findRolePermissions(1L)).thenReturn(List.of("OLD_PERMISSION"));
        customSessionWs().authenticate(asyncResponse, credentials);
        verify(asyncResponse).resume(responseCaptor.capture());
        AdminSession defaultSession = (AdminSession) responseCaptor.getValue().getEntity();

        // When (renew with updated permissions)
        when(adminUserDao.findById(adminUser.getId())).thenReturn(adminUser);
        when(adminRoleService.findRolePermissions(1L)).thenReturn(List.of("NEW_PERMISSION"));
        AdminSession renewedSession = customSessionWs().renew(defaultSession.getWebSessionToken());

        // Then
        CustomSession renewed = jwtSessionSigner.parseSession(renewedSession.getWebSessionToken(), CustomSession.class);
        CustomSession original = jwtSessionSigner.parseSession(defaultSession.getWebSessionToken(), CustomSession.class);
        assertThat(renewed.getHashedFingerprint()).isEqualTo(original.getHashedFingerprint());
        assertThat(renewed.getPermissions()).containsExactly("NEW_PERMISSION");
    }

    private CustomSessionWs customSessionWs() {
        return new CustomSessionWs(adminUserService, jwtSessionSigner, adminConfigurationService,
            adminSecurityConfigurationService, clock);
    }

    private AdminUser mockAdminUser(String password) {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(1L);
        adminUser.setIdRole(1L);
        adminUser.setFirstName("John");
        adminUser.setLastName("Doe");
        adminUser.setUserName("testUser");
        adminUser.setPassword(hashService.hashPassword(password));
        return adminUser;
    }

    static class CustomSessionWs extends AbstractSessionWs<CustomSession> {
        CustomSessionWs(AdminUserService adminUserService, JwtSessionSigner jwtSessionSigner,
                AdminConfigurationService configurationService,
                AdminSecurityConfigurationService adminSecurityConfigurationService, Clock clock) {
            super(adminUserService, jwtSessionSigner, configurationService, adminSecurityConfigurationService, clock,
                new WebSessionProvider<CustomSession>() {
                    @Override
                    public Class<CustomSession> sessionClass() {
                        return CustomSession.class;
                    }

                    @Override
                    public CustomSession createSession(AuthenticatedUser user, String hashedFingerprint) {
                        return new CustomSession()
                            .setUserId(user.getUser().getId())
                            .setUserName(user.getUser().getUserName())
                            .setPermissions(user.getPermissions())
                            .setHashedFingerprint(hashedFingerprint);
                    }
                });
        }

        @Override
        protected long authenticatedUserId(CustomSession parsedSession) {
            return parsedSession.getUserId();
        }

        @Override
        protected String hashedFingerprint(CustomSession parsedSession) {
            return parsedSession.getHashedFingerprint();
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class CustomSession implements WebSessionPermission {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long userId;
        private String userName;
        private Set<String> permissions;
        private String hashedFingerprint;
    }

}
