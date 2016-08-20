package com.coreoz.plume.admin.db.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@Entity
@Table(name = "plm_role_permission")
public class AdminRolePermission {

	@Id
	private AdminRolePermissionId id;

	@Getter
	@Setter
	@ToString
	@Accessors(chain = true)
	@Embeddable
	@EqualsAndHashCode
	public static class AdminRolePermissionId implements Serializable {
		private static final long serialVersionUID = -225702535241410480L;

		@Column(name = "id_role")
		private Long idRole;
		private String permission;
	}

}
