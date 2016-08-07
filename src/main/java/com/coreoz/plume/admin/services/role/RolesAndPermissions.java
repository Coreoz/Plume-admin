package com.coreoz.plume.admin.services.role;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(fluent = true)
public class RolesAndPermissions {
	private Set<String> permissionsAvailable;
	private List<RoleWithPermissions> rolesWithPermissions;

}
