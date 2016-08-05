package com.coreoz.plume.admin.webservices.security;

import java.util.Set;

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
public class SessionBasicInformation implements SessionInformation {

	private long userId;
	private String username;
	private Set<String> permissions;
	private long expirationTime;

}
