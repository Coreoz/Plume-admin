package com.coreoz.plume.admin.services.configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Singleton
public class AdminConfigurationService {

	private final Config config;

	@Inject
	public AdminConfigurationService(Config config) {
		// the reference file is not located in src/main/resources/ to ensure
		// that it is not overridden by another config file when a "fat jar" is created.
		this.config = config.withFallback(
			ConfigFactory.parseResources(AdminConfigurationService.class, "reference.conf")
		);
	}

	public String jwtSecret() {
		return config.getString("admin.jwt-secret");
	}

	public long sessionDurationInMillis() {
		return config.getDuration("admin.session-duration", TimeUnit.MILLISECONDS);
	}

	public int loginMaxAttempts() {
		return config.getInt("admin.login.max-attempts");
	}

	public Duration loginBlockedDuration() {
		return config.getDuration("admin.login.blocked-duration");
	}

	public int passwordsMinimumLength() {
		return config.getInt("admin.passwords.min-length");
	}

}
