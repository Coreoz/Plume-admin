package com.coreoz.plume.admin.websession;

public interface WebSessionSigner {

	/**
	 * Returns an instance of {@link #T} if the session could be read and is fully valid
	 * or null otherwise.
	 */
	<T extends WebSession> T parseSession(String sessionInformationSerialized, Class<T> sessionClass);

	/**
	 * Serialize into a string the session and sign it
	 */
	String serializeSession(Object sessionInformation);

}
