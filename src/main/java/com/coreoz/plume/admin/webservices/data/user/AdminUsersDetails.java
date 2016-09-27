package com.coreoz.plume.admin.webservices.data.user;

import java.util.List;

import com.coreoz.plume.admin.db.generated.AdminRole;

import lombok.Value;

@Value(staticConstructor = "of")
public class AdminUsersDetails {

	private final List<AdminUserDetails> users;
	private final List<AdminRole> roles;

}
