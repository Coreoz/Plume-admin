package com.coreoz.plume.admin.websession;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.services.time.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class JwtSessionSignerProvider implements Provider<JwtSessionSigner> {

	private final JwtSessionSigner jwtSessionSigner;

	@Inject
	public JwtSessionSignerProvider(AdminSecurityConfigurationService conf,
			ObjectMapper objectMapper, TimeProvider timeProvider) {
		this.jwtSessionSigner = new JwtSessionSigner(conf.jwtSecret(), objectMapper, timeProvider);
	}

	@Override
	public JwtSessionSigner get() {
		return jwtSessionSigner;
	}

}
