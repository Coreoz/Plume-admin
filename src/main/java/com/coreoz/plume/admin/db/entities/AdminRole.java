package com.coreoz.plume.admin.db.entities;

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
@Table(name = "pl_bo_role_permission")
public class AdminRole {

	@Id
	@GeneratedValue(generator = HibernateIdGenerator.NAME)
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	private String label;

}
