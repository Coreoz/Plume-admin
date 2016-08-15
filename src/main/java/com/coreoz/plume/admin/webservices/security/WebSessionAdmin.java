package com.coreoz.plume.admin.webservices.security;

import java.util.Set;

import com.coreoz.plume.admin.security.permission.WebSessionPermission;

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

	private long idUser;
	private String userName;
	private String fullName;
	private Set<String> permissions;
	private long expirationTime;

}
