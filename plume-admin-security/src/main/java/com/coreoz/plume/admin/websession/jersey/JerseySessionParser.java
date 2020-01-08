package com.coreoz.plume.admin.websession.jersey;

import java.nio.charset.StandardCharsets;
import java.util.function.BiPredicate;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;

import com.coreoz.plume.admin.websession.WebSessionFingerprint;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.google.common.hash.Hashing;
import com.google.common.net.HttpHeaders;

/**
 * Parse session from a {@link ContainerRequestContext} request
 */
public class JerseySessionParser {

	private static final BiPredicate<?, ?> ALWAYS_TRUE_BI_PREDICATE = (a, b) -> true;

	public static final String FINGERPRINT_COOKIE_NAME = "session-fgp";

	private static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";

	private static final String BEARER_PREFIX = "Bearer ";
	private static final Object EMPTY_SESSION = new Object();

	public static <T> T currentSessionInformation(ContainerRequestContext request,
			WebSessionSigner webSessionSigner, Class<T> webSessionClass) {
		return currentSessionInformationWithCheck(request, webSessionSigner, webSessionClass, alwaysTrueBiPredicate());
	}

	public static <T extends WebSessionFingerprint> T currentSessionInformationWithFingerprintCheck(
			ContainerRequestContext request, WebSessionSigner webSessionSigner, Class<T> webSessionClass) {
		return currentSessionInformationWithCheck(request, webSessionSigner, webSessionClass, JerseySessionParser::verifyFingerprintHash);
	}

	public static <T> T currentSessionInformation(ContainerRequestContext request,
			WebSessionSigner webSessionSigner, Class<T> webSessionClass, boolean should) {
		return currentSessionInformationWithCheck(request, webSessionSigner, webSessionClass, alwaysTrueBiPredicate());
	}

	public static String hashFingerprint(String fingerprint) {
		return Hashing.sha256().hashString(fingerprint, StandardCharsets.UTF_8).toString();
	}

	//	private

	@SuppressWarnings("unchecked")
	private static<T, U> BiPredicate<T, U> alwaysTrueBiPredicate() {
		return (BiPredicate<T, U>) ALWAYS_TRUE_BI_PREDICATE;
	}

	private static boolean verifyFingerprintHash(ContainerRequestContext request, WebSessionFingerprint webSessionFingerprint) {
		return verifyFingerprintHash(request, webSessionFingerprint.getHashedFingerprint());
	}

	private static boolean verifyFingerprintHash(ContainerRequestContext request, String hashedFingerprint) {
		Cookie fingerPrintCookie = request.getCookies().get(FINGERPRINT_COOKIE_NAME);
		if(fingerPrintCookie == null || fingerPrintCookie.getValue() == null) {
			return false;
		}

		return hashFingerprint(fingerPrintCookie.getValue()).equals(hashedFingerprint);
	}

	@SuppressWarnings("unchecked")
	private static <T> T currentSessionInformationWithCheck(ContainerRequestContext request,
			WebSessionSigner webSessionSigner, Class<T> webSessionClass,
			BiPredicate<ContainerRequestContext, T> checkFunction) {
		Object webSession = request.getProperty(REQUEST_SESSION_ATTRIBUTE_NAME);
		if (webSession == null) {
			String webSessionSerialized = parseAuthorizationBearer(request);
			if(webSessionSerialized != null) {
				T webSessionParsed = webSessionSigner.parseSession(webSessionSerialized, webSessionClass);
				if(webSessionParsed != null && checkFunction.test(request, webSessionParsed)) {
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

}
