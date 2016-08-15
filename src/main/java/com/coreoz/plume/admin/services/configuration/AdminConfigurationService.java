package com.coreoz.plume.admin.services.configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.typesafe.config.Config;

@Singleton
public class AdminConfigurationService {

	private final Config config;

	@Inject
	public AdminConfigurationService(Config config) {
		this.config = config;
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

}
