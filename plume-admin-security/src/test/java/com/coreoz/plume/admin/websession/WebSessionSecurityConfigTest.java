package com.coreoz.plume.admin.websession;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;

import static org.assertj.core.api.Assertions.assertThat;

class WebSessionSecurityConfigTest {

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void admin_config_should_use_web_session_admin_and_restrict_to_admin() {
		WebSessionSecurityConfig config = WebSessionSecurityConfig.admin();

		assertThat(config.sessionClass()).isEqualTo(WebSessionAdmin.class);
		assertThat(config.accessControlAnnotation()).isEqualTo(RestrictToAdmin.class);
		assertThat(config.permissionExtractor().apply(restrictToAdmin("MANAGE_USERS"))).isEqualTo("MANAGE_USERS");
		SessionFingerprintExtractor<WebSessionAdmin> fingerprintExtractor =
			(SessionFingerprintExtractor) config.fingerprintExtractor();
		assertThat(fingerprintExtractor.hashedFingerprint(new WebSessionAdmin().setHashedFingerprint("hash")))
			.isEqualTo("hash");
		assertThat(config.fingerprintCookieName()).isEqualTo("session-fgp");
	}

	@Test
	void of_should_disable_fingerprint_when_no_extractor_is_provided() {
		WebSessionSecurityConfig config = WebSessionSecurityConfig.of(
			CustomSession.class, RestrictToAdmin.class, RestrictToAdmin::value);

		assertThat(config.sessionClass()).isEqualTo(CustomSession.class);
		assertThat(config.fingerprintExtractor()).isNull();
	}

	private static RestrictToAdmin restrictToAdmin(String value) {
		return new RestrictToAdmin() {
			@Override
			public String value() {
				return value;
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return RestrictToAdmin.class;
			}
		};
	}

	@SuppressWarnings("unused")
	private static class CustomSession implements WebSessionPermission {
		@Override
		public Set<String> getPermissions() {
			return Set.of();
		}

		@Override
		public String getUserName() {
			return null;
		}
	}

}
