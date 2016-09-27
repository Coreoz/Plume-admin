package com.coreoz.plume.admin.services.user;

import java.util.Set;

import com.coreoz.plume.admin.db.generated.AdminUser;

import lombok.Value;

@Value(staticConstructor = "of")
public class AuthenticatedUser {

	private final AdminUser user;
	private final Set<String> permissions;

}
