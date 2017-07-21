package com.coreoz.plume.admin.webservices.validation;

import com.coreoz.plume.jersey.errors.WsError;

public enum AdminWsError implements WsError {

	WRONG_LOGIN_OR_PASSWORD,
	TOO_MANY_WRONG_ATTEMPS,
	PASSWORD_TOO_SHORT,
	PASSWORDS_DIFFERENT,
	EMAIL_ALREADY_EXISTS,
	USERNAME_ALREADY_EXISTS,
	ROLE_LABEL_EXISTS,

}
