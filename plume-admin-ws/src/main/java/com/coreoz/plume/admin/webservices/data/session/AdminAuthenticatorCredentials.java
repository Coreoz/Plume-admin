package com.coreoz.plume.admin.webservices.data.session;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminAuthenticatorCredentials {

	private String userName;
	private int code;

}
