package com.coreoz.plume.admin.websession;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.services.time.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class WebSessionSigner extends JwtSessionSigner<WebSessionPermission> {

	@Inject
	public WebSessionSigner(AdminSecurityConfigurationService conf,
			ObjectMapper objectMapper, TimeProvider timeProvider) {
		super(conf.jwtSecret(), objectMapper, timeProvider);
	}

}
