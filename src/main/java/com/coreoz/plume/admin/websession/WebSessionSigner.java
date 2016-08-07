package com.coreoz.plume.admin.websession;

public interface WebSessionSigner<T extends WebSession> {

	/**
	 * Returns an instance of {@link #T} if the session could be read and is fully valid
	 * or null otherwise.
	 */
	T parseSession(String sessionInformationSerialized);

	/**
	 * Serialize into a string the session and sign it
	 */
	String serializeSession(T sessionInformation);

}
