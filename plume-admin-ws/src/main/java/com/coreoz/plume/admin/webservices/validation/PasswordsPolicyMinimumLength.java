package com.coreoz.plume.admin.webservices.validation;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.jersey.errors.WsException;

/**
 * Check only that passwords are long enough
 */
@Singleton
public class PasswordsPolicyMinimumLength implements PasswordsPolicy {

	private final int minLength;

	@Inject
	public PasswordsPolicyMinimumLength(AdminConfigurationService configurationService) {
		this.minLength = configurationService.passwordsMinimumLength();
	}

	@Override
	public void checkPasswordSecure(String password) {
		if(password != null && password.length() < minLength) {
			throw new WsException(AdminWsError.PASSWORD_TOO_SHORT, String.valueOf(minLength));
		}
	}

}
