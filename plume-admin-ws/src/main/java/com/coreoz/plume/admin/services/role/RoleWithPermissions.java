package com.coreoz.plume.admin.services.role;

import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class RoleWithPermissions {
	@JsonSerialize(using = ToStringSerializer.class)
	private Long idRole;
	private String label;
	private Set<String> permissions;
}
