package com.coreoz.plume.admin.webservices.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.jersey.errors.WsException;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;

public class PasswordsPolicyMinimumLengthTest {

	@Test
	public void password_too_short_should_raise_an_error() {
		PasswordsPolicyMinimumLength policy = new PasswordsPolicyMinimumLength(
			new AdminConfigurationService(ConfigFactory.parseMap(ImmutableMap.of(
				"admin.passwords.min-length", "8"
			)))
		);

		try {
			policy.checkPasswordSecure("test");
			Assertions.fail("should raise an error");
		} catch (WsException e) {
			// as excepted, the password is too short
		}
	}

	@Test
	public void password_long_enough_should_not_raise_an_error() {
		PasswordsPolicyMinimumLength policy = new PasswordsPolicyMinimumLength(
			new AdminConfigurationService(ConfigFactory.parseMap(ImmutableMap.of(
				"admin.passwords.min-length", "8"
			)))
		);

		// should not raise an exception
		policy.checkPasswordSecure("test test");
	}

}
