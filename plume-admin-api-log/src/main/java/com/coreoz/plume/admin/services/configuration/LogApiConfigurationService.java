package com.coreoz.plume.admin.services.configuration;

import java.time.Duration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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

	public Long bodyMaxCharsDisplayed() {
		return config.getBytes("api.log.body-max-chars-displayed");
	}

	public int cleaningMaxLogsPerApi() {
		return config.getInt("api.log.cleaning.max-logs-per-api");
	}

	public Duration cleaningMaxDuration() {
		return config.getDuration("api.log.cleaning.max-duration");
	}

	public Duration cleaningRunningEvery() {
		return config.getDuration("api.log.cleaning.running-every");
	}

	public boolean saveToDatabase() {
		return config.getBoolean("api.log.save-to-database");
	}

	public Integer defaultLimit() {
		return config.getInt("api.log.default-limit");
	}

}
