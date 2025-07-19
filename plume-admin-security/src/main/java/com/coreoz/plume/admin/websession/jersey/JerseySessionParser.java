package com.coreoz.plume.admin.websession.jersey;

import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coreoz.plume.admin.websession.WebSessionFingerprint;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.google.common.hash.Hashing;
import com.google.common.net.HttpHeaders;

/**
 * Parse session from a {@link ContainerRequestContext} request
 */
public class JerseySessionParser {

	private static final Logger logger = LoggerFactory.getLogger(JerseySessionParser.class);

	private static final VerifyFingerprintFunction NO_VERIFY_FINGERPRINT_FUNCTION = (a, b, c) -> true;

	public static final String FINGERPRINT_COOKIE_NAME = "session-fgp";

	private static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";

	private static final String BEARER_PREFIX = "Bearer ";
	private static final Object EMPTY_SESSION = new Object();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T currentSessionInformation(ContainerRequestContext request,
			WebSessionSigner webSessionSigner, Class<T> webSessionClass) {
		return (T) currentSessionInformation(request, webSessionSigner, (Class) webSessionClass, false);
	}

	public static <T extends WebSessionFingerprint> T currentSessionInformationWithFingerprintCheck(
			ContainerRequestContext request, WebSessionSigner webSessionSigner, Class<T> webSessionClass) {
		return currentSessionInformation(request, webSessionSigner, webSessionClass, true);
	}

	public static <T extends WebSessionFingerprint> T currentSessionInformation(ContainerRequestContext request,
			WebSessionSigner webSessionSigner, Class<T> webSessionClass, boolean verifyCookieFingerprint) {
		return currentSessionInformation(request, webSessionSigner, webSessionClass, verifyCookieFingerprint, FINGERPRINT_COOKIE_NAME);
	}

	public static <T extends WebSessionFingerprint> T currentSessionInformation(ContainerRequestContext request,
			WebSessionSigner webSessionSigner, Class<T> webSessionClass, boolean verifyCookieFingerprint,
			String fingerprintCookieName) {
		return currentSessionInformationWithCheck(
			request,
			webSessionSigner,
			webSessionClass,
			verifyCookieFingerprint ?
				JerseySessionParser::verifyFingerprintHash
				: NO_VERIFY_FINGERPRINT_FUNCTION,
			fingerprintCookieName
		);
	}

	public static String hashFingerprint(String fingerprint) {
		return Hashing.sha256().hashString(fingerprint, StandardCharsets.UTF_8).toString();
	}

	//	private

	private static boolean verifyFingerprintHash(
			ContainerRequestContext request,
			WebSessionFingerprint webSessionFingerprint,
			String fingerprintCookieName) {
		return verifyFingerprintHash(request, webSessionFingerprint.getHashedFingerprint(), fingerprintCookieName);
	}

	private static boolean verifyFingerprintHash(ContainerRequestContext request, String hashedFingerprint, String fingerprintCookieName) {
		Cookie fingerprintCookie = request.getCookies().get(fingerprintCookieName);
		if(fingerprintCookie == null || fingerprintCookie.getValue() == null) {
			logger.warn("No fingerprint cookie provided (are you using HTTPS?), you can disable the "
					+ "admin.session.use-fingerprint-cookie parameter if that is an issue "
					+ "(though is lower the session security)");
			return false;
		}

		boolean isHashFingerprintValid = hashFingerprint(fingerprintCookie.getValue()).equals(hashedFingerprint);
		if(!isHashFingerprintValid) {
			logger.warn("The cookie fingerprint does not match the JWT token fingerprint hash, you can disable the "
				+ "admin.session.use-fingerprint-cookie parameter if that is an issue "
				+ "(though is lower the session security)");
		}

		return isHashFingerprintValid;
	}

	@SuppressWarnings("unchecked")
	private static <T extends WebSessionFingerprint> T currentSessionInformationWithCheck(ContainerRequestContext request,
			WebSessionSigner webSessionSigner, Class<T> webSessionClass,
			VerifyFingerprintFunction checkFunction, String fingerprintCookieName) {
		Object webSession = request.getProperty(REQUEST_SESSION_ATTRIBUTE_NAME);
		if (webSession == null) {
			String webSessionSerialized = parseAuthorizationBearer(request);
			if(webSessionSerialized != null) {
				T webSessionParsed = webSessionSigner.parseSession(webSessionSerialized, webSessionClass);
				if(webSessionParsed != null && checkFunction.verifyFingerprint(request, webSessionParsed, fingerprintCookieName)) {
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
	private static interface VerifyFingerprintFunction {
		boolean verifyFingerprint(
			ContainerRequestContext request,
			WebSessionFingerprint webSessionFingerprint,
			String fingerprintCookieName
		);
	}

}
