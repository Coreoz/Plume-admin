package com.coreoz.plume.admin.db.entities;

import java.io.Serializable;

import javax.persistence.Column;
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
@EqualsAndHashCode
@Entity
@Table(name = "plm_role_permission")
public class AdminRolePermission implements Serializable {

	private static final long serialVersionUID = -236137818170344047L;

	@Id
	@Column(name = "id_role")
	private Long idRole;
	@Id
	private String permission;

}
