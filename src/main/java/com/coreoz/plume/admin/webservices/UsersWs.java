package com.coreoz.plume.admin.webservices;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.coreoz.plume.admin.db.entities.AdminUser;
import com.coreoz.plume.admin.services.permissions.AdminPermissions;
import com.coreoz.plume.admin.services.user.AdminUserService;
import com.coreoz.plume.admin.webservices.data.user.AdminUserDetails;
import com.coreoz.plume.admin.webservices.data.user.AdminUserParameters;
import com.coreoz.plume.admin.webservices.errors.AdminWsError;
import com.coreoz.plume.jersey.errors.Validators;
import com.coreoz.plume.jersey.errors.WsException;
import com.coreoz.plume.jersey.security.RestrictTo;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/admin/users")
@Api(value = "Manage admin users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictTo(AdminPermissions.MANAGE_USERS)
@Singleton
public class UsersWs {

	private final AdminUserService adminUserService;

	@Inject
	public UsersWs(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@GET
	@ApiOperation(value = "Fetch add admin users")
	public List<AdminUserDetails> fetchAll() {
		return adminUserService
				.findAll()
				.stream()
				.map(this::toUserBoDetails)
				.collect(Collectors.toList());
	}

	@PUT
	@ApiOperation(value = "Update a user")
	public void update(AdminUserParameters parameters) {
		validateAdminUserParameters(parameters);
		Validators.checkRequired("users.ID", parameters.getId());

		adminUserService.update(parameters);
	}

	@POST
	@ApiOperation(value = "Add a user")
	public AdminUserDetails create(AdminUserParameters parameters) {
		validateAdminUserParameters(parameters);
		Validators.checkRequired("users.PASSWORD", parameters.getPassword());

		return toUserBoDetails(adminUserService.create(parameters));
	}

	@DELETE
	@Path("{userId}")
	@ApiOperation(value = "Supprime un utilisateur")
	public void delete(@PathParam("userId") long userId) {
		adminUserService.delete(userId);
	}

	private void validateAdminUserParameters(AdminUserParameters parameters) {
		Validators.checkRequired(parameters);
		Validators.checkRequired("users.EMAIL", parameters.getEmail());
		Validators.checkEmail("users.EMAIL", parameters.getEmail());
		Validators.checkRequired("users.USERNAME", parameters.getUserName());
		Validators.checkRequired("users.FIRSTNAME", parameters.getFirstName());
		Validators.checkRequired("users.LASTNAME", parameters.getLastName());
		Validators.checkRequired("users.ROLE", parameters.getIdRole());

		if (!Strings.isNullOrEmpty(parameters.getPassword()) && !parameters.getPassword().equals(parameters.getPasswordConfirmation())) {
			throw new WsException(
				AdminWsError.PASSWORDS_DIFFERENT,
				ImmutableList.of(
					"users.PASSWORD",
					"users.PASSWORD_CONFIRM"
				)
			);
		}

		if (adminUserService.existsWithUsername(parameters.getId(), parameters.getUserName())) {
			throw new WsException(AdminWsError.USERNAME_ALREADY_EXISTS);
		}
		if (adminUserService.existsWithEmail(parameters.getId(), parameters.getEmail())) {
			throw new WsException(AdminWsError.EMAIL_ALREADY_EXISTS);
		}
	}

	private AdminUserDetails toUserBoDetails(AdminUser user) {
		return new AdminUserDetails()
				.setCreationDate(user.getCreationDate())
				.setEmail(user.getEmail())
				.setFirstName(user.getFirstName())
				.setLastName(user.getLastName())
				.setIdRole(user.getIdRole())
				.setId(user.getId())
				.setUserName(user.getUserName());
	}

}