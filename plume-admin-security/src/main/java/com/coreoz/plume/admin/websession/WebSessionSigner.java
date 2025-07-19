package com.coreoz.plume.admin.websession;

/**
 * Enable serialization/deserialization of web session objects
 */
public interface WebSessionSigner {

	<T> T parseSession(String webSessionSerialized, Class<T> sessionClass);
	String serializeSession(Object sessionInformation, Long expirationTime);

}
