package com.coreoz.plume.admin.services.user;

import com.coreoz.plume.admin.services.configuration.AdminTimingSecurityConfigurationService;
import com.coreoz.plume.admin.services.hash.HashService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimedAuthenticationSecurerTest {

    @Test
    public void test_verifyPasswordAuthentication() throws ExecutionException, InterruptedException {
        AdminTimingSecurityConfigurationService configurationService = mock(AdminTimingSecurityConfigurationService.class);
        when(configurationService.enabled()).thenReturn(true);
        when(configurationService.movingAverageWindow()).thenReturn(2);
        when(configurationService.threadPoolSize()).thenReturn(1);

        HashService hashService = mock(HashService.class);
        when(hashService.checkPassword("password", "hashedPassword")).thenReturn(true);

        Clock clock = mock(Clock.class);

        TimedAuthenticationSecurer timedAuthenticationSecurer = new TimedAuthenticationSecurer(hashService, clock, configurationService);

        CompletableFuture<Optional<String>> result = timedAuthenticationSecurer.verifyPasswordAuthentication(
            Optional.of("user"),
            user -> "hashedPassword",
            "password"
        );

        assertThat(result.get()).isPresent();
    }
}
