package com.coreoz.plume.admin.db.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.coreoz.plume.db.hibernate.HibernateIdGenerator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "pl_bo_user")
public class AdminUser {

	@Id
	@GeneratedValue(generator = HibernateIdGenerator.NAME)
	@JsonSerialize(using = ToStringSerializer.class)
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
