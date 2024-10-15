package com.coreoz.plume.admin.webservices;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.coreoz.plume.admin.jersey.feature.RestrictToAdmin;
import com.coreoz.plume.admin.services.permissions.AdminPermissions;
import com.coreoz.plume.admin.services.role.AdminRoleService;
import com.coreoz.plume.admin.services.role.RoleWithPermissions;
import com.coreoz.plume.admin.services.role.RolesAndPermissions;
import com.coreoz.plume.admin.webservices.validation.AdminWsError;
import com.coreoz.plume.jersey.errors.Validators;
import com.coreoz.plume.jersey.errors.WsException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin/roles")
@Tag(name = "admin-roles", description = "Manage roles")
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
	@Operation(description = "Fetch all permissions available and the association between roles"
			+ " and permissions")
	public RolesAndPermissions permissions() {
		return roleService.findRoleWithPermissions();
	}

	@POST
	@Operation(description = "Create or update a role with its permissions")
	public RoleWithPermissions save(RoleWithPermissions roleWithPermissions) {
		Validators.checkRequired("roles.LABEL", roleWithPermissions.getLabel());

		if (roleService.existsWithLabel(roleWithPermissions.getIdRole(), roleWithPermissions.getLabel())) {
			throw new WsException(AdminWsError.ROLE_LABEL_EXISTS);
		}

		return roleService.saveWithPermissions(roleWithPermissions);
	}

	@Path("{idRole}")
	@DELETE
	@Operation(description = "Delete a role")
	public void delete(@PathParam("idRole") long idRole) {
		roleService.deleteWithPermissions(idRole);
	}

}
