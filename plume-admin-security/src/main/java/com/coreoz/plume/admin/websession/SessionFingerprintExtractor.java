package com.coreoz.plume.admin.websession;

/**
 * Extract the hashed fingerprint from a web session object.
 * <p>
 * This enables fingerprint verification to be used with any web session type,
 * without requiring the session to implement {@link WebSessionFingerprint}.
 * A {@code null} extractor means that fingerprint verification is disabled.
 *
 * @param <T> The web session type
 */
@FunctionalInterface
public interface SessionFingerprintExtractor<T> {

	/**
	 * @return The hashed fingerprint stored in the session, or {@code null} if there is none
	 */
	String hashedFingerprint(T session);

}
