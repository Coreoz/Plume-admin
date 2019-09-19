package com.coreoz.plume.admin.websession;

import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Contains secure data of a user
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class WebSessionAdmin implements WebSessionPermission {

	@JsonSerialize(using = ToStringSerializer.class)
	private long idUser;
	private String userName;
	private String fullName;
	private Set<String> permissions;

}
