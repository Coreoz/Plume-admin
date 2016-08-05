package com.coreoz.plume.admin.webservices;

import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.coreoz.plume.jersey.security.RestrictTo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/admin/roles")
@Api(value = "Gère les rôles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictTo(Permissions.MANAGE_USERS)
@Singleton
public class RoleWs {

	private final RoleService roleService;

	@Inject
	public RoleWs(RoleService roleService) {
		this.roleService = roleService;
	}

	@GET
	@ApiOperation(value = "Récupère les rôles")
	public RolesResult list() {
		RolesResult ret = new RolesResult();
		ret.setRoles(new ArrayList<>());
		for (RoleBo rbo : roleService.fetchAll()) {
			Role r = new Role();
			r.setId(Long.toString(rbo.getId()));
			r.setName(rbo.getLibelle());
			r.setPermissions(roleService.fetchRolePermissions(rbo.getId()));
			ret.getRoles().add(r);
		}

		ret.setPermissions(Permissions.values());

		return ret;
	}

	@POST
	@ApiOperation(value="Creer un role")
	public RolesResult save(List<Role> roles) {
		roleService.updateAll(roles);
		return list();
	}

}
