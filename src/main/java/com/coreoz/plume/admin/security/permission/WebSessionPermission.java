package com.coreoz.plume.admin.security.permission;

import java.util.Set;

import com.coreoz.plume.admin.websession.WebSession;

public interface WebSessionPermission extends WebSession {

	Set<String> getPermissions();
	String getUsername();

}
