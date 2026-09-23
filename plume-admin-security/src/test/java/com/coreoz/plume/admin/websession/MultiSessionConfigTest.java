package com.coreoz.plume.admin.websession;

import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.jersey.feature.WebSessionSecurityFeatures;
import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.jersey.guice.JerseyGuiceFeature;
import com.coreoz.plume.jersey.security.permission.RestrictTo;
import com.google.common.net.HttpHeaders;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultiSessionConfigTest extends JerseyTest {

	@Override
	protected Application configure() {
		WebSessionAdmin adminSession = new WebSessionAdmin()
			.setUserName("admin")
			.setPermissions(Set.of("ADMIN_PERM"))
			.setHashedFingerprint("hash");
		UserSession userSession = new UserSession()
			.setUserName("user")
			.setPermissions(Set.of("USER_PERM"))
			.setHashedFingerprint("hash");

		WebSessionSigner signer = mock(WebSessionSigner.class);
		when(signer.parseSession(anyString(), eq(WebSessionAdmin.class))).thenReturn(adminSession);
		when(signer.parseSession(anyString(), eq(UserSession.class))).thenReturn(userSession);

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
		config.register(WebSessionSecurityFeatures.of(
			WebSessionSecurityConfig.admin(),
			WebSessionSecurityConfig.of(
				UserSession.class, RestrictTo.class, RestrictTo::value, UserSession::getHashedFingerprint)
		));
		config.register(new JerseyGuiceFeature(injector));
		config.register(AdminResource.class);
		config.register(AdminForbiddenResource.class);
		config.register(UserResource.class);
		config.register(UserForbiddenResource.class);
		return config;
	}

	@Test
	void both_session_types_should_be_usable() {
		assertThat(get("admin")).isEqualTo("admin");
		assertThat(get("user")).isEqualTo("user");
	}

	@Test
	void both_access_control_filters_should_be_applied() {
		assertThat(status("admin-forbidden")).isEqualTo(403);
		assertThat(status("user-forbidden")).isEqualTo(403);
	}

	private String get(String path) {
		return target(path).request().header(HttpHeaders.AUTHORIZATION, "Bearer token").get(String.class);
	}

	private int status(String path) {
		return target(path).request().header(HttpHeaders.AUTHORIZATION, "Bearer token").get().getStatus();
	}

	@Path("admin")
	@RestrictToAdmin("ADMIN_PERM")
	public static class AdminResource {
		@GET
		public String get(@Context WebSessionAdmin session) {
			return session == null ? "null" : session.getUserName();
		}
	}

	@Path("admin-forbidden")
	@RestrictToAdmin("OTHER_PERM")
	public static class AdminForbiddenResource {
		@GET
		public String get() {
			return "forbidden";
		}
	}

	@Path("user")
	@RestrictTo("USER_PERM")
	public static class UserResource {
		@GET
		public String get(@Context UserSession session) {
			return session == null ? "null" : session.getUserName();
		}
	}

	@Path("user-forbidden")
	@RestrictTo("OTHER_PERM")
	public static class UserForbiddenResource {
		@GET
		public String get() {
			return "forbidden";
		}
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	public static class UserSession implements WebSessionPermission, WebSessionFingerprint {
		private String userName;
		private Set<String> permissions;
		private String hashedFingerprint;
	}

}
