package com.coreoz.plume.admin.webservices.session;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.guice.TestModule;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.services.session.AuthSessionTokenHandler;
import com.coreoz.plume.admin.services.session.WebSessionConfig;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.webservices.session.data.AdminCredentials;
import com.coreoz.plume.admin.webservices.session.data.AdminSession;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.jersey.JerseySessionParser;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@GuiceTest(TestModule.class)
class PasswordSessionWsTest {
    @Inject
    private AdminUserService adminUserService;
    @Inject
    private AdminUserDao adminUserDao;
    @Inject
    private AdminRoleService adminRoleService;
    @Inject
    private HashService hashService;
    @Inject
    private AuthSessionTokenHandler authSessionTokenHandler;
    @Inject
    private AdminConfigurationService adminConfigurationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_should_create_a_custom_session() {
        // Given
        String password = "password";
        AdminUser adminUser = mockAdminUser(password);
        AdminCredentials credentials = credentials(adminUser, password);
        AsyncResponse asyncResponse = mock(AsyncResponse.class);
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        CustomSessionWs customSessionWs = customSessionWs();

        // When
        when(adminUserDao.findByUserName(adminUser.getUserName())).thenReturn(Optional.of(adminUser));
        when(adminRoleService.findRolePermissions(1L)).thenReturn(List.of("PERMISSION"));
        customSessionWs.authenticate(asyncResponse, credentials);

        // Then
        verify(asyncResponse).resume(responseCaptor.capture());
        AdminSession session = (AdminSession) responseCaptor.getValue().getEntity();
        CustomSession customSession = authSessionTokenHandler
            .readUserSession(session.getWebSessionToken(), new CustomSessionConfig())
            .orElseThrow();

        assertThat(customSession.getUserId()).isEqualTo(1L);
        assertThat(customSession.getUserName()).isEqualTo("testUser");
        assertThat(customSession.getPermissions()).containsExactly("PERMISSION");
    }

    @Test
    void authenticate_should_emit_a_fingerprint_cookie_matching_the_session_hash() {
        // Given
        String password = "password";
        AdminUser adminUser = mockAdminUser(password);
        AsyncResponse asyncResponse = mock(AsyncResponse.class);
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        CustomSessionWs customSessionWs = customSessionWs();

        // When
        when(adminUserDao.findByUserName(adminUser.getUserName())).thenReturn(Optional.of(adminUser));
        when(adminRoleService.findRolePermissions(1L)).thenReturn(List.of("PERMISSION"));
        customSessionWs.authenticate(asyncResponse, credentials(adminUser, password));

        // Then
        verify(asyncResponse).resume(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        String fingerprintCookie = response.getHeaderString("Set-Cookie");
        assertThat(fingerprintCookie).startsWith(JerseySessionParser.FINGERPRINT_COOKIE_NAME + "=");

        String fingerprint = fingerprintCookie.split(";")[0]
            .substring(JerseySessionParser.FINGERPRINT_COOKIE_NAME.length() + 1);
        CustomSession customSession = authSessionTokenHandler
            .readUserSession(((AdminSession) response.getEntity()).getWebSessionToken(), new CustomSessionConfig())
            .orElseThrow();
        assertThat(customSession.getHashedFingerprint())
            .isEqualTo(JerseySessionParser.hashFingerprint(fingerprint));
    }

    @Test
    void renew_should_rotate_fingerprint_and_update_permissions() {
        // Given
        String password = "password";
        AdminUser adminUser = mockAdminUser(password);
        AsyncResponse asyncResponse = mock(AsyncResponse.class);
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        CustomSessionWs customSessionWs = customSessionWs();

        // When (initial authentication)
        when(adminUserDao.findByUserName(adminUser.getUserName())).thenReturn(Optional.of(adminUser));
        when(adminRoleService.findRolePermissions(1L)).thenReturn(List.of("OLD_PERMISSION"));
        customSessionWs.authenticate(asyncResponse, credentials(adminUser, password));
        verify(asyncResponse).resume(responseCaptor.capture());
        AdminSession defaultSession = (AdminSession) responseCaptor.getValue().getEntity();

        // When (renew with updated permissions)
        when(adminUserDao.findById(adminUser.getId())).thenReturn(adminUser);
        when(adminRoleService.findRolePermissions(1L)).thenReturn(List.of("NEW_PERMISSION"));
        Response renewedResponse = customSessionWs.renew(defaultSession.getWebSessionToken());

        // Then
        CustomSession renewed = authSessionTokenHandler
            .readUserSession(((AdminSession) renewedResponse.getEntity()).getWebSessionToken(), new CustomSessionConfig())
            .orElseThrow();
        CustomSession original = authSessionTokenHandler
            .readUserSession(defaultSession.getWebSessionToken(), new CustomSessionConfig())
            .orElseThrow();
        assertThat(renewed.getHashedFingerprint()).isNotEqualTo(original.getHashedFingerprint());
        assertThat(renewedResponse.getHeaderString("Set-Cookie")).isNotNull();
        assertThat(renewed.getPermissions()).containsExactly("NEW_PERMISSION");
    }

    @Test
    void renew_should_reject_an_invalid_token() {
        // Given
        CustomSessionWs customSessionWs = customSessionWs();

        // When / Then
        assertThatThrownBy(() -> customSessionWs.renew("invalid-token"))
            .isInstanceOf(com.coreoz.plume.jersey.errors.WsException.class);
    }

    private CustomSessionWs customSessionWs() {
        return new CustomSessionWs(adminUserService, adminConfigurationService, authSessionTokenHandler);
    }

    private static AdminCredentials credentials(AdminUser adminUser, String password) {
        AdminCredentials credentials = new AdminCredentials();
        credentials.setUserName(adminUser.getUserName());
        credentials.setPassword(password);
        return credentials;
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

    static class CustomSessionWs extends PasswordSessionWs<CustomSession> {
        CustomSessionWs(AdminUserService adminUserService, AdminConfigurationService configurationService,
                AuthSessionTokenHandler authSessionTokenHandler) {
            super(adminUserService, configurationService, authSessionTokenHandler, new CustomSessionConfig());
        }

        // Each web-service declares its own contract
        public Response renew(String webSessionSerialized) {
            return renewSession(webSessionSerialized);
        }
    }

    static class CustomSessionConfig implements WebSessionConfig<CustomSession> {
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

        @Override
        public long authenticatedUserId(CustomSession parsedSession) {
            return parsedSession.getUserId();
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
