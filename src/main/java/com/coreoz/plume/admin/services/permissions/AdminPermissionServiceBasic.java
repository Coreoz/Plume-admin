package com.coreoz.plume.admin.services.permissions;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class AdminPermissionServiceBasic implements AdminPermissionService {

	private final Set<String> permissionsAvailable = ImmutableSet.of(
			AdminPermissions.MANAGE_USERS,
			AdminPermissions.MANAGE_ROLES
		);

	@Override
	public Set<String> permissionsAvailable() {
		return permissionsAvailable;
	}

}
