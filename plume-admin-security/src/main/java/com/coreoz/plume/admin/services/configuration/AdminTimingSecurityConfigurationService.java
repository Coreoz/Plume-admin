package com.coreoz.plume.admin.services.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AdminTimingSecurityConfigurationService {

    private final Config config;

    @Inject
    public AdminTimingSecurityConfigurationService(Config config) {
        // the reference file is not loaded by default, so we need to load it
        this.config = config.withFallback(ConfigFactory.load("application.conf"));
    }

    public boolean enabled() {
        return config.getBoolean("admin.security-timing.enabled");
    }

    public int movingAverageWindow() {
        return config.getInt("admin.security-timing.moving-average-window");
    }

    public int threadPoolSize() {
        return config.getInt("admin.security-timing.thread-pool-size");
    }
}
