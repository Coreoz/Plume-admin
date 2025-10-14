package com.coreoz.plume.admin.guice;

import com.coreoz.plume.admin.db.daos.AdminRoleDao;
import com.coreoz.plume.admin.db.daos.AdminRolePermissionDao;
import com.coreoz.plume.admin.db.daos.AdminUserDao;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.services.time.TimeProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import org.mockito.Mockito;

import java.time.Duration;

/**
 * The Guice module that will be used for integration tests.
 * <p>
 * In this module, it is possible to override the behaviors of some services as it is shown below.
 */
public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        install(Modules.override(new GuiceAdminWsWithDefaultsModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                // Object mapper
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                bind(ObjectMapper.class).toInstance(objectMapper);

                // Configuration
                AdminSecurityConfigurationService adminSecurityConfigurationService = Mockito.mock(AdminSecurityConfigurationService.class);
                Mockito.when(adminSecurityConfigurationService.jwtSecret()).thenReturn("32<#pfs]Z£a<>5@W3TFX5.t)h{)C%,QQO~=[ZN!~17xf@j'\\@[");
                bind(AdminSecurityConfigurationService.class).toInstance(adminSecurityConfigurationService);
                AdminConfigurationService AdminConfigurationService = Mockito.mock(AdminConfigurationService.class);
                Mockito.when(AdminConfigurationService.loginMaxAttempts()).thenReturn(5);
                Mockito.when(AdminConfigurationService.loginBlockedDuration()).thenReturn(Duration.ofSeconds(5));
                Mockito.when(AdminConfigurationService.sessionRefreshDurationInMillis()).thenReturn(5000L);
                Mockito.when(AdminConfigurationService.sessionExpireDurationInMillis()).thenReturn(15000L);
                Mockito.when(AdminConfigurationService.sessionInactiveDurationInMillis()).thenReturn(120000L);
                Mockito.when(AdminConfigurationService.loginTimeingProtectorMaxSamples()).thenReturn(10);
                Mockito.when(AdminConfigurationService.loginTimeingProtectorSamplingRate()).thenReturn(0.22);
                bind(AdminConfigurationService.class).toInstance(AdminConfigurationService);

                // Services & DAOs
                bind(AdminRoleDao.class).toInstance(Mockito.mock(AdminRoleDao.class));
                bind(AdminRolePermissionDao.class).toInstance(Mockito.mock(AdminRolePermissionDao.class));
                bind(AdminUserDao.class).toInstance(Mockito.mock(AdminUserDao.class));
                bind(AdminRoleService.class).toInstance(Mockito.mock(AdminRoleService.class));
            }
        }));
    }
}
