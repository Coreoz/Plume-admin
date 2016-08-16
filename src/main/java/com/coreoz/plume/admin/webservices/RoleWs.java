package com.coreoz.plume.admin.webservices;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.coreoz.plume.admin.services.permissions.AdminPermissions;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.services.role.RoleWithPermissions;
import com.coreoz.plume.admin.services.role.RolesAndPermissions;
import com.coreoz.plume.admin.webservices.errors.AdminWsError;
import com.coreoz.plume.admin.webservices.security.RestrictToAdmin;
import com.coreoz.plume.jersey.errors.Validators;
import com.coreoz.plume.jersey.errors.WsException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/admin/roles")
@Api(value = "Manage roles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictToAdmin(AdminPermissions.MANAGE_ROLES)
@Singleton
public class RoleWs {

	private final AdminRoleService roleService;

	@Inject
	public RoleWs(AdminRoleService roleService) {
		this.roleService = roleService;
	}

	@GET
	@ApiOperation(value = "Fetch all permissions available and the association between roles"
			+ " and permissions")
	public RolesAndPermissions permissions() {
		return roleService.findRoleWithPermissions();
	}

	@POST
	@ApiOperation(value="Create or update a role with its permissions")
	public RoleWithPermissions save(RoleWithPermissions roleWithPermissions) {
		Validators.checkRequired("roles.LABEL", roleWithPermissions.getLabel());

		if (roleService.existsWithLabel(roleWithPermissions.getIdRole(), roleWithPermissions.getLabel())) {
			throw new WsException(AdminWsError.ROLE_LABEL_EXISTS);
		}

		return roleService.saveWithPermissions(roleWithPermissions);
	}

	@Path("{idRole}")
	@DELETE
	@ApiOperation(value="Delete a role")
	public void delete(@PathParam("idRole") long idRole) {
		roleService.deleteWithPermissions(idRole);
	}

}
