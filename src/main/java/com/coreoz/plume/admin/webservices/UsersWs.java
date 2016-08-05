package com.coreoz.plume.admin.webservices;

import java.security.Permissions;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.coreoz.plume.jersey.errors.Validators;
import com.coreoz.plume.jersey.errors.WsException;
import com.coreoz.plume.jersey.security.RestrictTo;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/admin/users")
@Api(value = "Gère les utilisateurs back-office")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RestrictTo(Permissions.MANAGE_USERS)
@Singleton
public class UsersWs {

	private final UtilisateurBoService utilisateurBoService;
	private final RoleService roleService;
	private final LieuService lieuService;

	@Inject
	public UsersWs(UtilisateurBoService utilisateurBoService, RoleService roleService, LieuService lieuService) {
		this.utilisateurBoService = utilisateurBoService;
		this.roleService = roleService;
		this.lieuService = lieuService;
	}

	@GET
	@ApiOperation(value = "Récupère les utilisateurs connectés")
	public List<UserBo> fetchAll() {
		return utilisateurBoService
				.fetchAll()
				.stream()
				.map(user -> UserBo.of(
						user.getId(),
						user.getPrenom(),
						user.getNom(),
						user.getEmail(),
						user.getIdentifiant()
				))
				.collect(Collectors.toList());
	}

	@GET
	@Path("/{userId}")
	@ApiOperation(value = "Récupère un utilisateur")
	public UserBoDetails details(@PathParam("userId") long userId) {
		return utilisateurBoService
				.findByIdOptional(userId)
				.map(this::toUserBoDetails)
				.orElseThrow(NotFoundException::new);
	}

	@PUT
	@ApiOperation(value = "Met à jour un utilisateur")
	public void update(UserBoParameter parameters) {
		Validators.checkRequired(parameters);
		Validators.checkRequired("users.IDENTIFIER", parameters.getId());
		Validators.checkRequired("users.EMAIL", parameters.getEmail());
		Validators.checkRequired("users.IDENTIFIER", parameters.getIdentifiant());
		Validators.checkRequired("users.LASTNAME", parameters.getNom());
		Validators.checkRequired("users.FIRSTNAME", parameters.getPrenom());
		Validators.checkRequired("users.ROLE", parameters.getRoleId());

		if (!Strings.isNullOrEmpty(parameters.getMotDePasse()) && !parameters.getMotDePasse().equals(parameters.getConfirmationMotDePasse())) {
			throw new WsException(
					BrcWsError.NEW_PASSWORDS_DIFFERENT,
					ImmutableList.of(
							"users.PASSWORD",
							"users.PASSWORD_CONFIRM"
					)
			);
		}

		if (utilisateurBoService.existsWithIdentifier(parameters.getId(), parameters.getIdentifiant())) {
			throw new WsException(BrcWsError.IDENTIFIER_ALREADY_EXISTS);
		}
		if (utilisateurBoService.existsWithEmail(parameters.getId(), parameters.getEmail())) {
			throw new WsException(BrcWsError.EMAIL_ALREADY_EXISTS);
		}

		// TODO vérifier l'existance du rôle

		utilisateurBoService.update(parameters);
	}

	@POST
	@ApiOperation(value = "Met à jour un utilisateur")
	public UserBoDetails create(UserBoParameter parameters) {
		Validators.checkRequired(parameters);
		Validators.checkRequired("users.EMAIL", parameters.getEmail());
		Validators.checkRequired("users.IDENTIFIER", parameters.getIdentifiant());
		Validators.checkRequired("users.LASTNAME", parameters.getNom());
		Validators.checkRequired("users.FIRSTNAME", parameters.getPrenom());
		Validators.checkRequired("users.ROLE", parameters.getRoleId());
		Validators.checkRequired("users.PASSWORD", parameters.getMotDePasse());

		if (!parameters.getMotDePasse().equals(parameters.getConfirmationMotDePasse())) {
			throw new WsException(
					BrcWsError.NEW_PASSWORDS_DIFFERENT,
					ImmutableList.of(
							"users.PASSWORD",
							"users.PASSWORD_CONFIRM"
					)
			);
		}

		if (utilisateurBoService.existsWithIdentifier(-1L, parameters.getIdentifiant())) {
			throw new WsException(BrcWsError.IDENTIFIER_ALREADY_EXISTS);
		}
		if (utilisateurBoService.existsWithEmail(-1L, parameters.getEmail())) {
			throw new WsException(BrcWsError.EMAIL_ALREADY_EXISTS);
		}

		// TODO vérifier l'existance du rôle

		return toUserBoDetails(utilisateurBoService.create(parameters));
	}

	@DELETE
	@Path("{userId}")
	@ApiOperation(value = "Supprime un utilisateur")
	public Response delete(@PathParam("userId") long userId) {
		return Response.ok().entity(utilisateurBoService.deleteUser(userId)).build();
	}

	private UserBoDetails toUserBoDetails(UtilisateurBo user) {
		return UserBoDetails.of(
				user.getId(),
				user.getPrenom(),
				user.getNom(),
				user.getEmail(),
				user.getIdentifiant(),
				user.getDateCreation(),
				user.getIdRole(),
				utilisateurBoService.getPistes(user.getId()),
				user.getCodePrestataire()
		);
	}

	@GET
	@Path("/pistes")
	@ApiOperation(value = "Recuperer la liste des pistes")
	public List<IdNameBean> pistes() {
		return lieuService.getList().stream().map(l -> IdNameBean.of(Long.toString(l.getIdLieu()), l.getNomLieu())).collect(Collectors.toList());
	}

}