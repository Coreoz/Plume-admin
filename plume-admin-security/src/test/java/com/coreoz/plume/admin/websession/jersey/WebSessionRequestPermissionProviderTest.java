package com.coreoz.plume.admin.websession.jersey;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.coreoz.plume.admin.websession.SessionFingerprintExtractor;
import com.coreoz.plume.admin.websession.WebSessionFingerprint;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.google.common.net.HttpHeaders;

import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebSessionRequestPermissionProviderTest {

	private static final TestSession SESSION = new TestSession()
		.setUserName("john")
		.setPermissions(Set.of("MANAGE_USERS"))
		.setHashedFingerprint("hash");

	private final WebSessionSigner signer = new WebSessionSigner() {
		@Override
		@SuppressWarnings("unchecked")
		public <T> T parseSession(String webSessionSerialized, Class<T> sessionClass) {
			return (T) SESSION;
		}

		@Override
		public String serializeSession(Object sessionInformation, Long expirationTime) {
			return null;
		}
	};

	@Test
	void should_extract_permissions_from_a_fingerprint_session_without_fingerprint_check() {
		// no fingerprint cookie on the request, but verification is disabled
		WebSessionRequestPermissionProvider<TestSession> provider = new WebSessionRequestPermissionProvider<>(
			signer, TestSession.class, false);

		assertThat(provider.correspondingPermissions(request())).containsExactly("MANAGE_USERS");
		assertThat(provider.userInformation(request())).isEqualTo("john");
	}

	@Test
	void should_reject_fingerprint_session_when_cookie_is_missing() {
		WebSessionRequestPermissionProvider<TestSession> provider = new WebSessionRequestPermissionProvider<>(
			signer, TestSession.class, true);

		assertThat(provider.correspondingPermissions(request())).isEmpty();
	}

	@Test
	void should_support_session_type_without_fingerprint() {
		PlainSession plainSession = new PlainSession().setUserName("jane").setPermissions(Set.of("BASIC"));
		WebSessionSigner plainSigner = new WebSessionSigner() {
			@Override
			@SuppressWarnings("unchecked")
			public <T> T parseSession(String webSessionSerialized, Class<T> sessionClass) {
				return (T) plainSession;
			}

			@Override
			public String serializeSession(Object sessionInformation, Long expirationTime) {
				return null;
			}
		};

		WebSessionRequestPermissionProvider<PlainSession> provider = new WebSessionRequestPermissionProvider<>(
			plainSigner, PlainSession.class, null, true);

		assertThat(provider.correspondingPermissions(request())).containsExactly("BASIC");
	}

	private ContainerRequestContext request() {
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");
		when(request.getCookies()).thenReturn(Map.of());
		return request;
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	static class TestSession implements WebSessionPermission, WebSessionFingerprint {
		private String userName;
		private Set<String> permissions;
		private String hashedFingerprint;
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	static class PlainSession implements WebSessionPermission {
		private String userName;
		private Set<String> permissions;
	}

}
