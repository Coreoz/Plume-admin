package com.coreoz.plume.admin.webservices.data.user;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class AdminUserDetails {

	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	@JsonSerialize(using = ToStringSerializer.class)
	private Long idRole;
	private LocalDateTime creationDate;
	private String firstName;
	private String lastName;
	private String email;
	private String userName;

}
