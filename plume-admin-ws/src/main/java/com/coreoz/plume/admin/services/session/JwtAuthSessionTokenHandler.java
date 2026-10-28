package com.coreoz.plume.admin.services.session;

import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.admin.services.configuration.AdminSecurityConfigurationService;
import com.coreoz.plume.admin.services.user.AuthenticatedUser;
import com.coreoz.plume.admin.webservices.session.data.AdminSession;
import com.coreoz.plume.admin.websession.JwtSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.jersey.JerseySessionParser;
import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Optional;

/**
 * Default {@link AuthSessionTokenHandler}: serializes the session as a JWT and, when enabled,
 * emits a fingerprint cookie on each login/renew.
 * <p>
 * The fingerprint is generated on every call to
 * {@link #generateUserSession(AuthenticatedUser, WebSessionConfig)}: only its hash is stored
 * inside the session (and therefore the signed token), while the clear value is sent to the
 * client in the fingerprint cookie.
 */
@Singleton
public class JwtAuthSessionTokenHandler implements AuthSessionTokenHandler {

    private static final SecureRandom FINGERPRINT_GENERATOR = new SecureRandom();
    private static final FingerprintWithHash NULL_FINGERPRINT = new FingerprintWithHash(null, null);

    private final JwtSessionSigner jwtSessionSigner;
    private final Clock clock;

    private final long maxTimeSessionDurationInMilliseconds;
    private final long sessionRefreshDurationInMillis;
    private final long sessionInactiveDurationInMillis;

    private final boolean sessionUseFingerprintCookie;
    private final boolean sessionFingerprintCookieHttpsOnly;

    @Inject
    public JwtAuthSessionTokenHandler(
        JwtSessionSigner jwtSessionSigner,
        AdminConfigurationService configurationService,
        AdminSecurityConfigurationService adminSecurityConfigurationService,
        Clock clock
    ) {
        this.jwtSessionSigner = jwtSessionSigner;
        this.clock = clock;

        this.maxTimeSessionDurationInMilliseconds = configurationService.sessionExpireDurationInMillis();
        this.sessionRefreshDurationInMillis = configurationService.sessionRefreshDurationInMillis();
        this.sessionInactiveDurationInMillis = configurationService.sessionInactiveDurationInMillis();

        this.sessionUseFingerprintCookie = adminSecurityConfigurationService.sessionUseFingerprintCookie();
        this.sessionFingerprintCookieHttpsOnly = adminSecurityConfigurationService.sessionFingerprintCookieHttpsOnly();
    }

    @Override
    public <T extends WebSessionPermission> Response generateUserSession(AuthenticatedUser user, WebSessionConfig<T> config) {
        FingerprintWithHash fingerprintWithHash = sessionUseFingerprintCookie ? generateFingerprint() : NULL_FINGERPRINT;
        T webSession = config.createSession(user, fingerprintWithHash.getHash());

        ResponseBuilder response = Response.ok(new AdminSession(
            jwtSessionSigner.serializeSession(
                webSession,
                clock.millis() + maxTimeSessionDurationInMilliseconds
            ),
            sessionRefreshDurationInMillis,
            sessionInactiveDurationInMillis
        ));

        if (sessionUseFingerprintCookie) {
            withFingerprintCookie(response, fingerprintWithHash.getFingerprint());
        }
        return response.build();
    }

    @Override
    public <T extends WebSessionPermission> Optional<T> readUserSession(String webSessionSerialized, WebSessionConfig<T> config) {
        return Optional.ofNullable(jwtSessionSigner.parseSession(webSessionSerialized, config.sessionClass()));
    }

    private ResponseBuilder withFingerprintCookie(ResponseBuilder response, String fingerprint) {
        return response.header(
            HttpHeaders.SET_COOKIE,
            JerseySessionParser.FINGERPRINT_COOKIE_NAME + "=" + fingerprint + "; path=/; SameSite=Strict; HttpOnly"
                + (sessionFingerprintCookieHttpsOnly ? "; Secure" : "")
        );
    }

    private static FingerprintWithHash generateFingerprint() {
        byte[] generatedFingerprintBytes = new byte[50];
        FINGERPRINT_GENERATOR.nextBytes(generatedFingerprintBytes);
        String generatedFingerprint = BaseEncoding.base16().encode(generatedFingerprintBytes);
        return new FingerprintWithHash(
            generatedFingerprint,
            JerseySessionParser.hashFingerprint(generatedFingerprint)
        );
    }

    @AllArgsConstructor
    @Getter
    static class FingerprintWithHash {
        private final String fingerprint;
        private final String hash;
    }

}
