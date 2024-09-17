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

    // Can be used as an issue name to create QR Code for MFA
    public String appName() {
        return config.getString("admin.app-name");
    }

	public long sessionExpireDurationInMillis() {
		return config.getDuration("admin.session.expire-duration", TimeUnit.MILLISECONDS);
	}

	public long sessionRefreshDurationInMillis() {
		long expireDuration = sessionExpireDurationInMillis();
		long refreshDuration = config.getDuration("admin.session.refresh-duration", TimeUnit.MILLISECONDS);
		if(expireDuration <= refreshDuration) {
			throw new RuntimeException(
				"Refresh duration (admin.session.refresh-duration), "
				+ "must be lower than the expire duration (admin.session.expire-duration)"
			);
		}
		return refreshDuration;
	}

	public long sessionInactiveDurationInMillis() {
		long expireDuration = sessionExpireDurationInMillis();
		long inactiveDuration = config.getDuration("admin.session.inactive-duration", TimeUnit.MILLISECONDS);
		if(expireDuration > inactiveDuration) {
			throw new RuntimeException(
				"Inactive duration (admin.session.inactive-duration), "
				+ "must be greater than the expire duration (admin.session.expire-duration)"
			);
		}
		return inactiveDuration;
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
