package com.coreoz.plume.admin.websession;

import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import com.coreoz.plume.admin.jersey.feature.WebSessionSecurityFeatures;
import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.jersey.guice.JerseyGuiceFeature;
import com.google.common.net.HttpHeaders;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebSessionSecurityConfigIntegrationTest extends JerseyTest {

	@Override
	protected Application configure() {
		WebSessionAdmin expected = new WebSessionAdmin()
			.setUserName("john")
			.setPermissions(Set.of("PERMISSION"))
			.setHashedFingerprint("hash");

		WebSessionSigner signer = mock(WebSessionSigner.class);
		when(signer.parseSession(anyString(), eq(WebSessionAdmin.class))).thenReturn(expected);

		AdminSecurityConfigurationService configurationService = mock(AdminSecurityConfigurationService.class);
		when(configurationService.sessionUseFingerprintCookie()).thenReturn(false);

		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(WebSessionSigner.class).toInstance(signer);
				bind(AdminSecurityConfigurationService.class).toInstance(configurationService);
			}
		});

		ResourceConfig config = new ResourceConfig();
		// The feature is registered before the Guice bridge, as in a classic Plume project
		config.register(WebSessionSecurityFeatures.of(WebSessionSecurityConfig.admin()));
		config.register(new JerseyGuiceFeature(injector));
		config.register(SessionResource.class);
		return config;
	}

	@Test
	void the_session_should_be_injectable_with_context() {
		String userName = target("session/me")
			.request()
			.header(HttpHeaders.AUTHORIZATION, "Bearer token")
			.get(String.class);

		assertThat(userName).isEqualTo("john");
	}

	@Path("session")
	public static class SessionResource {
		@GET
		@Path("me")
		public String me(@Context WebSessionAdmin webSessionAdmin) {
			return webSessionAdmin == null ? "null" : webSessionAdmin.getUserName();
		}
	}

}
