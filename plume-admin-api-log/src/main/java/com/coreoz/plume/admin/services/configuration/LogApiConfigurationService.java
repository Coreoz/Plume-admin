package com.coreoz.plume.admin.services.configuration;

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

    public int getLogBodyLimit(){ return config.getInt("api.log.bodyLimit");}

    public int getLogNumberMax() { return config.getInt("api.log.numberMax");}

    public int getLogNumberDaysLimit() { return config.getInt("api.log.numberDaysLimit");}

    public String getLogTiming() {return config.getString("api.log.clean.timing");}

    public long getLogCleanDelay() {return config.getLong("api.log.clean.url.timing");}
}
