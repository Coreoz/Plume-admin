package com.coreoz.plume.admin.webservices.validation;

import com.coreoz.plume.jersey.errors.WsException;

/**
 * Enable to check passwords against security rules.<br>
 * <br>
 * Before implementing a new passwords policy checker, one should at least read:
 * <ul>
 * <li>http://xkcd.com/936/</li>
 * <li>https://nakedsecurity.sophos.com/2016/08/18/nists-new-password-rules-what-you-need-to-know/</li>
 * </ul>
 */
public interface PasswordsPolicy {

	/**
	 * Check that the password comply security policies.
	 * @throws WsException If the passwords does not comply security policies.
	 */
	void checkPasswordSecure(String password);

}
