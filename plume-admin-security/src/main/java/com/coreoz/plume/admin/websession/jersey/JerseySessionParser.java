package com.coreoz.plume.admin.websession.jersey;

import com.coreoz.plume.admin.websession.SessionFingerprintExtractor;
import com.coreoz.plume.admin.websession.WebSessionFingerprint;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.google.common.hash.Hashing;
import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Cookie;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * Parse session from a {@link ContainerRequestContext} request
 */
@Slf4j
public class JerseySessionParser {
    private static final VerifyFingerprintFunction<Object> NO_VERIFY_FINGERPRINT_FUNCTION = (a, b, c) -> true;

    public static final String FINGERPRINT_COOKIE_NAME = "session-fgp";

    private static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Object EMPTY_SESSION = new Object();

    private JerseySessionParser() {
        // static class
    }

    public static <T> T currentSessionInformation(
        ContainerRequestContext request,
        WebSessionSigner webSessionSigner,
        Class<T> webSessionClass
    ) {
        return currentSessionInformation(
            request,
            webSessionSigner,
            webSessionClass,
            null,
            false,
            FINGERPRINT_COOKIE_NAME
        );
    }

    public static <T extends WebSessionFingerprint> T currentSessionInformationWithFingerprintCheck(
        ContainerRequestContext request,
        WebSessionSigner webSessionSigner,
        Class<T> webSessionClass
    ) {
        return currentSessionInformation(
            request,
            webSessionSigner,
            webSessionClass,
            true
        );
    }

    public static <T extends WebSessionFingerprint> T currentSessionInformation(
        ContainerRequestContext request,
        WebSessionSigner webSessionSigner,
        Class<T> webSessionClass,
        boolean verifyCookieFingerprint
    ) {
        return currentSessionInformation(
            request,
            webSessionSigner,
            webSessionClass,
            verifyCookieFingerprint,
            FINGERPRINT_COOKIE_NAME
        );
    }

    public static <T extends WebSessionFingerprint> T currentSessionInformation(
        ContainerRequestContext request,
        WebSessionSigner webSessionSigner,
        Class<T> webSessionClass,
        boolean verifyCookieFingerprint,
        String fingerprintCookieName
    ) {
        return currentSessionInformation(
            request,
            webSessionSigner,
            webSessionClass,
            WebSessionFingerprint::getHashedFingerprint,
            verifyCookieFingerprint,
            fingerprintCookieName
        );
    }

    /**
     * Parse the session from the request and, if requested, verify its fingerprint using the
     * provided extractor. This works with any session type, whether it implements
     * {@link WebSessionFingerprint} or not.
     *
     * @param fingerprintExtractor    Extract the hashed fingerprint from the session, or {@code null} to disable verification
     * @param verifyCookieFingerprint Whether the fingerprint cookie must be verified (ignored when no extractor is provided)
     * @param fingerprintCookieName   The name of the fingerprint cookie
     */
    public static <T> T currentSessionInformation(
        ContainerRequestContext request,
        WebSessionSigner webSessionSigner,
        Class<T> webSessionClass,
        SessionFingerprintExtractor<T> fingerprintExtractor,
        boolean verifyCookieFingerprint,
        String fingerprintCookieName
    ) {
        VerifyFingerprintFunction<T> checkFunction = verifyCookieFingerprint && fingerprintExtractor != null ?
            (req, session, cookieName) -> verifyFingerprintHash(req, fingerprintExtractor.hashedFingerprint(session), cookieName)
            : noVerifyFingerprintFunction();
        return currentSessionInformationWithCheck(
            request,
            webSessionSigner,
            webSessionClass,
            checkFunction,
            fingerprintCookieName
        );
    }

    public static String hashFingerprint(String fingerprint) {
        return Hashing.sha256().hashString(fingerprint, StandardCharsets.UTF_8).toString();
    }

    //	private

    @SuppressWarnings("unchecked")
    private static <T> VerifyFingerprintFunction<T> noVerifyFingerprintFunction() {
        return (VerifyFingerprintFunction<T>) NO_VERIFY_FINGERPRINT_FUNCTION;
    }

    private static boolean verifyFingerprintHash(ContainerRequestContext request, String hashedFingerprint, String fingerprintCookieName) {
        Cookie fingerprintCookie = request.getCookies().get(fingerprintCookieName);
        if (fingerprintCookie == null || fingerprintCookie.getValue() == null) {
            log.warn("No fingerprint cookie provided (are you using HTTPS?), you can disable the "
                + "admin.session.use-fingerprint-cookie parameter if that is an issue "
                + "(though is lower the session security)");
            return false;
        }

        boolean isHashFingerprintValid = hashFingerprint(fingerprintCookie.getValue()).equals(hashedFingerprint);
        if (!isHashFingerprintValid) {
            log.warn("The cookie fingerprint does not match the JWT token fingerprint hash, you can disable the "
                + "admin.session.use-fingerprint-cookie parameter if that is an issue "
                + "(though is lower the session security)");
        }

        return isHashFingerprintValid;
    }

    @SuppressWarnings("unchecked")
    private static <T> T currentSessionInformationWithCheck(
        ContainerRequestContext request,
        WebSessionSigner webSessionSigner,
        Class<T> webSessionClass,
        VerifyFingerprintFunction<T> checkFunction,
        String fingerprintCookieName
    ) {
        Object webSession = request.getProperty(REQUEST_SESSION_ATTRIBUTE_NAME);
        if (webSession == null) {
            String webSessionSerialized = parseAuthorizationBearer(request);
            if (webSessionSerialized != null) {
                T webSessionParsed = webSessionSigner.parseSession(webSessionSerialized, webSessionClass);
                if (
                    webSessionParsed != null
                        && checkFunction.verifyFingerprint(request, webSessionParsed, fingerprintCookieName)
                ) {
                    webSession = webSessionParsed;
                }
            }
            if (webSession == null) {
                webSession = EMPTY_SESSION;
            }
            request.setProperty(REQUEST_SESSION_ATTRIBUTE_NAME, webSession);
        }
        return webSession == EMPTY_SESSION ? null : (T) webSession;
    }

    private static String parseAuthorizationBearer(ContainerRequestContext request) {
        String authorization = request.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    @FunctionalInterface
    private interface VerifyFingerprintFunction<T> {
        boolean verifyFingerprint(
            ContainerRequestContext request,
            T webSession,
            String fingerprintCookieName
        );
    }

}
