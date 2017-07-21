package com.coreoz.plume.admin.websession;

import java.util.Set;

public interface WebSessionPermission {

	Set<String> getPermissions();
	String getUserName();

}
