package com.coreoz.plume.admin.services.permissions;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class AdminPermissionServiceBasic implements AdminPermissionService {

	@Override
	public Set<String> permissionsAvailable() {
		return ImmutableSet.of(AdminPermissions.MANAGE_USERS, AdminPermissions.MANAGE_ROLES);
	}

}
