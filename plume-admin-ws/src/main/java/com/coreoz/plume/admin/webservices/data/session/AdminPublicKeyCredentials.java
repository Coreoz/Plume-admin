package com.coreoz.plume.admin.webservices.data.session;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminPublicKeyCredentials {

	private AdminCredentials credentials;
	private String publicKeyCredentialJson;

}
