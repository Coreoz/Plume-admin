package com.coreoz.plume.admin.webservices;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.coreoz.plume.admin.db.entities.AdminRole;
import com.coreoz.plume.admin.services.permissions.AdminPermissions;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.services.role.RolesAndPermissions;
import com.coreoz.plume.jersey.security.RestrictTo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/admin/roles")
@Api(value = "Manage roles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictTo(AdminPermissions.MANAGE_ROLES)
@Singleton
public class RoleWs {

	private final AdminRoleService roleService;

	@Inject
	public RoleWs(AdminRoleService roleService) {
		this.roleService = roleService;
	}

	@GET
	@ApiOperation(value = "Fetch all available roles")
	public List<AdminRole> roles() {
		return roleService.findAll();
	}

	@Path("/permissions")
	@GET
	@ApiOperation(value = "Fetch all permissions available and the association between roles"
			+ " and permissions")
	public RolesAndPermissions permissions() {
		return roleService.findRoleWithPermissions();
	}

//	@POST
//	@ApiOperation(value="Creer un role")
//	public RolesResult save(List<Role> roles) {
//		roleService.updateAll(roles);
//		return list();
//	}

}
