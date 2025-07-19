package com.coreoz.plume.admin.websession;

import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.time.Clock;

@Singleton
public class JwtSessionSignerProvider implements Provider<JwtSessionSigner> {

	private final JwtSessionSigner jwtSessionSigner;

	@Inject
	public JwtSessionSignerProvider(AdminSecurityConfigurationService conf,
			ObjectMapper objectMapper, Clock clock) {
		this.jwtSessionSigner = new JwtSessionSigner(conf.jwtSecret(), objectMapper, clock);
	}

	@Override
	public JwtSessionSigner get() {
		return jwtSessionSigner;
	}

}
