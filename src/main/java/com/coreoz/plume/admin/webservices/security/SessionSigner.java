package com.coreoz.plume.admin.webservices.security;

public interface SessionSigner {

	/**
	 * Returns an instance of sessionInformationClass if the session could be read and is fully valid
	 * or null otherwise.
	 */
	<T extends SessionInformation> T parseSession(String sessionInformationSerialized, Class<T> sessionInformationClass);

	/**
	 * Serialize into a string the session and sign it
	 */
	<T extends SessionInformation> String serializeSession(T sessionInformation);

}
