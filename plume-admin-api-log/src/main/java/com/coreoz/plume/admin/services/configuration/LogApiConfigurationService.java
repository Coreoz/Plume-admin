package com.coreoz.plume.admin.services.configuration;

import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Singleton
public class LogApiConfigurationService {

    private final Config config;

    @Inject
    public LogApiConfigurationService(Config config) {
        // the reference file is not located in src/main/resources/ to ensure
        // that it is not overridden by another config file when a "fat jar" is created.
        this.config = config.withFallback(
            ConfigFactory.parseResources(LogApiConfigurationService.class, "reference.conf")
        );
    }

    public Long bodyMaxBytesDisplayed() {
    	return config.getBytes("body-max-bytes-displayed");
    }

    public int cleaningMaxLogsPerApi() {
    	return config.getInt("api.log.cleaning.max-logs");
    }

    public Duration cleaningMaxDuration() {
    	return config.getDuration("api.log.cleaning.max-duration");
    }

    public Duration cleaningRunningEvery() {
    	return config.getDuration("api.log.cleaning.running-every");
    }

}
