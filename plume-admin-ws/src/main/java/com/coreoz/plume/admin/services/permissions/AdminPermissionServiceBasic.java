package com.coreoz.plume.admin.services.permissions;

import java.util.Set;

public class AdminPermissionServiceBasic implements AdminPermissionService {

	private final Set<String> permissionsAvailable = Set.of(
			AdminPermissions.MANAGE_USERS,
			AdminPermissions.MANAGE_ROLES
		);

	@Override
	public Set<String> permissionsAvailable() {
		return permissionsAvailable;
	}

}
