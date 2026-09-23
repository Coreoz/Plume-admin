package com.coreoz.plume.admin.websession;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.websession.jersey.JerseySessionParser;

/**
 * Describes how a web session is secured:
 * <ul>
 *   <li>the session object type (parsed from the JWT token),</li>
 *   <li>the access control annotation used to protect resources,</li>
 *   <li>how to extract the required permission from this annotation,</li>
 *   <li>optionally, how to extract the session fingerprint (or {@code null} to disable fingerprint verification).</li>
 * </ul>
 * Configs are registered in Jersey through
 * {@link com.coreoz.plume.admin.jersey.feature.WebSessionSecurityFeatures}:
 * <pre>
 * config.register(WebSessionSecurityFeatures.of(
 *     WebSessionSecurityConfig.admin(),
 *     WebSessionSecurityConfig.of(
 *         MyWebSession.class, RestrictTo.class, RestrictTo::value, MyWebSession::getHashedFingerprint)
 * ));
 * </pre>
 */
public final class WebSessionSecurityConfig {

	private final Class<? extends WebSessionPermission> sessionClass;
	private final Class<? extends Annotation> accessControlAnnotation;
	private final Function<Annotation, String> permissionExtractor;
	private final SessionFingerprintExtractor<? extends WebSessionPermission> fingerprintExtractor;
	private final String fingerprintCookieName;

	private WebSessionSecurityConfig(
			Class<? extends WebSessionPermission> sessionClass,
			Class<? extends Annotation> accessControlAnnotation,
			Function<Annotation, String> permissionExtractor,
			SessionFingerprintExtractor<? extends WebSessionPermission> fingerprintExtractor,
			String fingerprintCookieName) {
		this.sessionClass = sessionClass;
		this.accessControlAnnotation = accessControlAnnotation;
		this.permissionExtractor = permissionExtractor;
		this.fingerprintExtractor = fingerprintExtractor;
		this.fingerprintCookieName = fingerprintCookieName;
	}

	/**
	 * Create a config without fingerprint verification.
	 */
	public static <A extends Annotation, T extends WebSessionPermission> WebSessionSecurityConfig of(
			Class<T> sessionClass, Class<A> accessControlAnnotation, Function<A, String> permissionExtractor) {
		return of(sessionClass, accessControlAnnotation, permissionExtractor, null);
	}

	/**
	 * Create a config with an optional fingerprint extractor. A {@code null} extractor disables
	 * fingerprint verification.
	 */
	@SuppressWarnings("unchecked")
	public static <A extends Annotation, T extends WebSessionPermission> WebSessionSecurityConfig of(
			Class<T> sessionClass, Class<A> accessControlAnnotation, Function<A, String> permissionExtractor,
			SessionFingerprintExtractor<T> fingerprintExtractor) {
		return new WebSessionSecurityConfig(
			sessionClass,
			accessControlAnnotation,
			(Function<Annotation, String>) permissionExtractor,
			fingerprintExtractor,
			JerseySessionParser.FINGERPRINT_COOKIE_NAME
		);
	}

	/**
	 * Create a config using a custom fingerprint cookie name.
	 */
	public WebSessionSecurityConfig withFingerprintCookieName(String fingerprintCookieName) {
		return new WebSessionSecurityConfig(
			sessionClass,
			accessControlAnnotation,
			permissionExtractor,
			fingerprintExtractor,
			fingerprintCookieName
		);
	}

	/**
	 * The default administration configuration: {@link WebSessionAdmin} sessions protected by
	 * {@link RestrictToAdmin} permissions, with fingerprint verification.
	 */
	public static WebSessionSecurityConfig admin() {
		return of(
			WebSessionAdmin.class,
			RestrictToAdmin.class,
			RestrictToAdmin::value,
			WebSessionAdmin::getHashedFingerprint
		);
	}

	public Class<? extends WebSessionPermission> sessionClass() {
		return sessionClass;
	}

	public Class<? extends Annotation> accessControlAnnotation() {
		return accessControlAnnotation;
	}

	public Function<Annotation, String> permissionExtractor() {
		return permissionExtractor;
	}

	public SessionFingerprintExtractor<? extends WebSessionPermission> fingerprintExtractor() {
		return fingerprintExtractor;
	}

	public String fingerprintCookieName() {
		return fingerprintCookieName;
	}

}
