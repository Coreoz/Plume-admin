package com.coreoz.plume.admin.webservices.data.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserParameters {

	private Long id;
	private Long idRole;
	private String firstName;
	private String lastName;
	private String email;
	private String userName;
	private String password;
	private String passwordConfirmation;

}
