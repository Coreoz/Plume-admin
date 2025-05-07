package com.coreoz.plume.admin.webservices;

import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.guice.TestModule;
import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.webservices.data.session.AdminCredentials;
import com.coreoz.plume.admin.webservices.data.session.AdminSession;
import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.test.GuiceTest;
import com.google.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private JwtSessionSigner jwtSessionSigner;

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

        // When
        when(adminUserDao.findByUserName(adminUser.getUserName())).thenReturn(Optional.of(adminUser));
        when(adminRoleService.findRolePermissions(1L)).thenReturn(permissions);
        AdminSession session = (AdminSession) sessionWs.authenticate(credentials).getEntity();

        // Then
        WebSessionAdmin webSessionAdmin = jwtSessionSigner.parseSession(session.getWebSessionToken(), WebSessionAdmin.class);
        assertThat(webSessionAdmin.getPermissions()).containsAll(permissions);
        assertThat(session.getRefreshDurationInMillis()).isEqualTo(5000L);
        assertThat(session.getInactiveDurationInMillis()).isEqualTo(120000L);
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

        // When
        when(adminUserDao.findByUserName(adminUser.getUserName())).thenReturn(Optional.of(adminUser));
        when(adminRoleService.findRolePermissions(1L)).thenReturn(defaultPermissions);
        AdminSession defaultSession = (AdminSession) sessionWs.authenticate(credentials).getEntity();

        when(adminUserDao.findById(adminUser.getId())).thenReturn(adminUser);
        when(adminRoleService.findRolePermissions(1L)).thenReturn(updatedPermissions);
        AdminSession session  = sessionWs.renew(defaultSession.getWebSessionToken());

        // Then
        WebSessionAdmin defaultSessionAdmin = jwtSessionSigner.parseSession(session.getWebSessionToken(), WebSessionAdmin.class);
        WebSessionAdmin webSessionAdmin = jwtSessionSigner.parseSession(session.getWebSessionToken(), WebSessionAdmin.class);
        assertThat(webSessionAdmin.getHashedFingerprint()).isEqualTo(defaultSessionAdmin.getHashedFingerprint());
        assertThat(webSessionAdmin.getPermissions()).containsAll(updatedPermissions);
        for (String defaultPermission : defaultPermissions) {
            assertThat(webSessionAdmin.getPermissions()).doesNotContain(defaultPermission);
        }
        assertThat(session.getRefreshDurationInMillis()).isEqualTo(5000L);
        assertThat(session.getInactiveDurationInMillis()).isEqualTo(120000L);
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
