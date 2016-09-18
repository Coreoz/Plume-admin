package com.coreoz.plume.admin.db.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.coreoz.plume.db.hibernate.utils.HibernateIdGenerator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@Entity
@Table(name = "plm_role")
public class AdminRole {

	@Id
	@GeneratedValue(generator = HibernateIdGenerator.NAME)
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	private String label;

}
