package com.coreoz.plume.admin.services.permissions;

import java.util.Set;

public interface AdminPermissionService {

	/**
	 * Returns all the application permissions available.
	 * Must include at least MANAGE_USERS and MANAGE_ROLES
	 */
	Set<String> permissionsAvailable();

}
