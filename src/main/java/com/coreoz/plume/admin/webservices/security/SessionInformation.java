package com.coreoz.plume.admin.webservices.security;

import java.util.Set;

public interface SessionInformation {

	long getExpirationTime();
	Set<String> getPermissions();
	String getUsername();

}
