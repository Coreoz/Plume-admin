package com.coreoz.plume.admin.db.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.coreoz.plume.db.hibernate.HibernateIdGenerator;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@Entity
@Table(name = "pl_bo_user")
public class AdminUser {

	@Id
	@GeneratedValue(generator = HibernateIdGenerator.NAME)
	private Long id;
	@Column(name = "id_role")
	private Long idRole;
	@Column(name = "creation_date")
	private LocalDateTime creationDate;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;
	private String email;
	@Column(name = "user_name")
	private String userName;
	private String password;

}
