package com.coreoz.plume.admin.webservices.session;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.coreoz.plume.admin.db.generated.AdminUser;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.services.session.AdminWebSessionConfig;
import com.coreoz.plume.admin.services.session.AuthSessionTokenHandler;
import com.coreoz.plume.admin.services.session.JwtAuthSessionTokenHandler;
import com.coreoz.plume.admin.services.session.WebSessionConfig;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.webservices.session.data.AdminSession;
import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionAdmin;
import com.coreoz.plume.admin.websession.jersey.JerseySessionParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthSessionTokenHandlerTest {

    private static final String JWT_SECRET = "32<#pfs]Z£a<>5@W3TFX5.t)h{)C%,QQO~=[ZN!~17xf@j'\\@[";
    private static final long EXPIRE_DURATION_IN_MILLIS = 15_000L;
    private static final WebSessionConfig<WebSessionAdmin> CONFIG = new AdminWebSessionConfig();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private AdminSecurityConfigurationService securityConfiguration;
    private AuthSessionTokenHandler handler;

    @BeforeEach
    void setUp() {
        securityConfiguration = mock(AdminSecurityConfigurationService.class);
        when(securityConfiguration.jwtSecret()).thenReturn(JWT_SECRET);
        when(securityConfiguration.sessionUseFingerprintCookie()).thenReturn(true);
        when(securityConfiguration.sessionFingerprintCookieHttpsOnly()).thenReturn(false);
        handler = newHandler();
    }

    @Test
    void generateUserSession_should_emit_token_and_fingerprint_cookie() {
        // Given
        AuthenticatedUser user = authenticatedUser();

        // When
        Response response = handler.generateUserSession(user, CONFIG);

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        AdminSession session = (AdminSession) response.getEntity();
        assertThat(session.getWebSessionToken()).isNotBlank();
        assertThat(session.getRefreshDurationInMillis()).isEqualTo(5000L);
        assertThat(session.getInactiveDurationInMillis()).isEqualTo(120_000L);

        String cookie = response.getHeaderString("Set-Cookie");
        assertThat(cookie).startsWith(JerseySessionParser.FINGERPRINT_COOKIE_NAME + "=")
            .contains("path=/", "SameSite=Strict", "HttpOnly")
            .doesNotContain("Secure");

        String clearFingerprint = fingerprintFromCookie(cookie);
        WebSessionAdmin stored = handler.readUserSession(session.getWebSessionToken(), CONFIG).orElseThrow();
        assertThat(stored.getHashedFingerprint())
            .isEqualTo(JerseySessionParser.hashFingerprint(clearFingerprint));
        assertThat(stored.getHashedFingerprint()).isNotEqualTo(clearFingerprint);
    }

    @Test
    void generateUserSession_should_add_secure_flag_when_https_only() {
        // Given
        when(securityConfiguration.sessionFingerprintCookieHttpsOnly()).thenReturn(true);
        handler = newHandler();

        // When
        Response response = handler.generateUserSession(authenticatedUser(), CONFIG);

        // Then
        assertThat(response.getHeaderString("Set-Cookie")).contains("; Secure");
    }

    @Test
    void generateUserSession_should_not_emit_cookie_when_disabled() {
        // Given
        when(securityConfiguration.sessionUseFingerprintCookie()).thenReturn(false);
        handler = newHandler();

        // When
        Response response = handler.generateUserSession(authenticatedUser(), CONFIG);

        // Then
        assertThat(response.getHeaderString("Set-Cookie")).isNull();
        WebSessionAdmin stored = handler.readUserSession(((AdminSession) response.getEntity()).getWebSessionToken(), CONFIG)
            .orElseThrow();
        assertThat(stored.getHashedFingerprint()).isNull();
    }

    @Test
    void generateUserSession_should_rotate_fingerprint_on_each_call() {
        // When
        Response first = handler.generateUserSession(authenticatedUser(), CONFIG);
        Response second = handler.generateUserSession(authenticatedUser(), CONFIG);

        // Then
        assertThat(fingerprintFromCookie(first.getHeaderString("Set-Cookie")))
            .isNotEqualTo(fingerprintFromCookie(second.getHeaderString("Set-Cookie")));
        assertThat(first.getHeaderString("Set-Cookie"))
            .isNotEqualTo(second.getHeaderString("Set-Cookie"));
    }

    @Test
    void readUserSession_should_return_the_stored_session() {
        // Given
        Response response = handler.generateUserSession(authenticatedUser(), CONFIG);
        String token = ((AdminSession) response.getEntity()).getWebSessionToken();

        // When
        Optional<WebSessionAdmin> parsed = handler.readUserSession(token, CONFIG);

        // Then
        assertThat(parsed).isPresent();
        assertThat(parsed.get().getUserName()).isEqualTo("testUser");
        assertThat(parsed.get().getPermissions()).containsExactly("PERMISSION");
    }

    @Test
    void readUserSession_should_return_empty_on_tampered_token() {
        assertThat(handler.readUserSession("not-a-valid-jwt", CONFIG)).isEmpty();
    }

    @Test
    void readUserSession_should_return_empty_on_expired_token() {
        // Given: a token that expires in the past relative to the reading clock
        AuthSessionTokenHandler expiredIssuer =
            newAuthSessionTokenHandler(Clock.fixed(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC));
        String token = ((AdminSession) expiredIssuer.generateUserSession(authenticatedUser(), CONFIG).getEntity())
            .getWebSessionToken();

        // When
        Optional<WebSessionAdmin> parsed = handler.readUserSession(token, CONFIG);

        // Then
        assertThat(parsed).isEmpty();
    }

    private AuthSessionTokenHandler newHandler() {
        return newAuthSessionTokenHandler(CLOCK);
    }

    private AuthSessionTokenHandler newAuthSessionTokenHandler(Clock clock) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        AdminConfigurationService configuration = mock(AdminConfigurationService.class);
        when(configuration.sessionExpireDurationInMillis()).thenReturn(EXPIRE_DURATION_IN_MILLIS);
        when(configuration.sessionRefreshDurationInMillis()).thenReturn(5000L);
        when(configuration.sessionInactiveDurationInMillis()).thenReturn(120_000L);

        return new JwtAuthSessionTokenHandler(
            new JwtSessionSigner(securityConfiguration.jwtSecret(), objectMapper, clock),
            configuration,
            securityConfiguration,
            clock
        );
    }

    private static AuthenticatedUser authenticatedUser() {
        AdminUser user = new AdminUser();
        user.setId(1L);
        user.setUserName("testUser");
        user.setFirstName("John");
        user.setLastName("Doe");
        return new AuthenticatedUser() {
            @Override
            public AdminUser getUser() {
                return user;
            }

            @Override
            public Set<String> getPermissions() {
                return Set.copyOf(List.of("PERMISSION"));
            }
        };
    }

    private static String fingerprintFromCookie(String cookie) {
        return cookie.split(";")[0].substring(JerseySessionParser.FINGERPRINT_COOKIE_NAME.length() + 1);
    }

}
