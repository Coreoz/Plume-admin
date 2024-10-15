package com.coreoz.plume.admin.services.configuration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Singleton
public class AdminSecurityConfigurationService {

	private final Config config;

	@Inject
	public AdminSecurityConfigurationService(Config config) {
		// the reference file is not located in src/main/resources/ to ensure
		// that it is not overridden by another config file when a "fat jar" is created.
		this.config = config.withFallback(
			ConfigFactory.parseResources(AdminSecurityConfigurationService.class, "reference.conf")
		);
	}

	public String jwtSecret() {
		return config.getString("admin.jwt-secret");
	}

	public boolean sessionUseFingerprintCookie() {
		return config.getBoolean("admin.session.use-fingerprint-cookie");
	}

	public boolean sessionFingerprintCookieHttpsOnly() {
		return config.getBoolean("admin.session.fingerprint-cookie-https-only");
	}

}
