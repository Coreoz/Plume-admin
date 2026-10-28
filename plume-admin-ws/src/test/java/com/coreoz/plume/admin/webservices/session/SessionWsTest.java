package com.coreoz.plume.admin.webservices.session;

import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.guice.TestModule;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.services.session.AdminWebSessionConfig;
import com.coreoz.plume.admin.services.session.AuthSessionTokenHandler;
import com.coreoz.plume.admin.webservices.session.data.AdminCredentials;
import com.coreoz.plume.admin.webservices.session.data.AdminSession;
import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.test.GuiceTest;
import com.google.inject.Inject;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@GuiceTest(TestModule.class)
class SessionWsTest {
    @Inject
    private SessionWs sessionWs;
    @Inject
    private AdminUserDao adminUserDao;
    @Inject
    private AdminRoleService adminRoleService;
    @Inject
    private HashService hashService;
    @Inject
    private AuthSessionTokenHandler authSessionTokenHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_should_create_session_with_all_permissions() {
        // Given
        String password = "password";
        List<String> permissions = List.of("PERMISSION");
        AdminUser adminUser = mockAdminUser(password);
        AdminCredentials credentials = new AdminCredentials();
        credentials.setUserName(adminUser.getUserName());
        credentials.setPassword(password);

        // Mocking the async response and preparing to capture its result
        AsyncResponse asyncResponse = mock(AsyncResponse.class);
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        // When
        when(adminUserDao.findByUserName(adminUser.getUserName())).thenReturn(Optional.of(adminUser));
        when(adminRoleService.findRolePermissions(1L)).thenReturn(permissions);
        sessionWs.authenticate(asyncResponse, credentials);

        // Then
        // Verify that the resume method was called and capture the response
        verify(asyncResponse).resume(responseCaptor.capture());
        Response capturedResponse = responseCaptor.getValue();
        AdminSession session = (AdminSession) capturedResponse.getEntity();

        WebSessionAdmin webSessionAdmin = authSessionTokenHandler
            .readUserSession(session.getWebSessionToken(), new AdminWebSessionConfig())
            .orElseThrow();
        assertThat(webSessionAdmin.getPermissions()).containsAll(permissions);
        assertThat(webSessionAdmin.getHashedFingerprint()).isNotBlank();
        assertThat(session.getRefreshDurationInMillis()).isEqualTo(5000L);
        assertThat(session.getInactiveDurationInMillis()).isEqualTo(120000L);
    }

    @Test
    void authenticate_should_set_a_secure_fingerprint_cookie_according_to_configuration() {
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
        sessionWs.authenticate(asyncResponse, credentials);

        // Then
        verify(asyncResponse).resume(responseCaptor.capture());
        String cookie = responseCaptor.getValue().getHeaderString("Set-Cookie");
        assertThat(cookie).startsWith("session-fgp=")
            .contains("path=/")
            .contains("SameSite=Strict")
            .contains("HttpOnly")
            .doesNotContain("Secure");
    }

    @Test
    void renew_should_update_permissions_in_session() {
        // Given
        String password = "password";
        List<String> defaultPermissions = List.of("PERMISSION");
        List<String> updatedPermissions = List.of("UPDATED_PERMISSION");
        AdminUser adminUser = mockAdminUser(password);
        AdminCredentials credentials = new AdminCredentials();
        credentials.setUserName(adminUser.getUserName());
        credentials.setPassword(password);

        // Mocking the async response and preparing to capture its result for the initial authentication
        AsyncResponse asyncResponse = mock(AsyncResponse.class);
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);

        // When (Phase 1: Authenticate to get a session token)
        when(adminUserDao.findByUserName(adminUser.getUserName())).thenReturn(Optional.of(adminUser));
        when(adminRoleService.findRolePermissions(1L)).thenReturn(defaultPermissions);
        sessionWs.authenticate(asyncResponse, credentials);

        // Verify that the resume method was called and capture the response
        verify(asyncResponse).resume(responseCaptor.capture());
        Response capturedResponse = responseCaptor.getValue();
        AdminSession defaultSession = (AdminSession) capturedResponse.getEntity();

        // When (Phase 2: Renew the token with updated permissions)
        when(adminUserDao.findById(adminUser.getId())).thenReturn(adminUser);
        when(adminRoleService.findRolePermissions(1L)).thenReturn(updatedPermissions);
        Response renewedResponse = sessionWs.renew(defaultSession.getWebSessionToken());
        AdminSession session = (AdminSession) renewedResponse.getEntity();

        // Then
        assertThat(renewedResponse.getHeaderString("Set-Cookie")).isNotNull();
        WebSessionAdmin defaultSessionAdmin = authSessionTokenHandler
            .readUserSession(defaultSession.getWebSessionToken(), new AdminWebSessionConfig())
            .orElseThrow();
        WebSessionAdmin webSessionAdmin = authSessionTokenHandler
            .readUserSession(session.getWebSessionToken(), new AdminWebSessionConfig())
            .orElseThrow();
        assertThat(webSessionAdmin.getHashedFingerprint()).isNotEqualTo(defaultSessionAdmin.getHashedFingerprint());
        assertThat(webSessionAdmin.getPermissions()).containsAll(updatedPermissions);
        for (String defaultPermission : defaultPermissions) {
            assertThat(webSessionAdmin.getPermissions()).doesNotContain(defaultPermission);
        }
        assertThat(session.getRefreshDurationInMillis()).isEqualTo(5000L);
        assertThat(session.getInactiveDurationInMillis()).isEqualTo(120000L);
    }

    @Test
    void renew_should_reject_an_expired_or_invalid_token() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> sessionWs.renew("not-a-valid-token"))
            .isInstanceOf(com.coreoz.plume.jersey.errors.WsException.class);
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
}
