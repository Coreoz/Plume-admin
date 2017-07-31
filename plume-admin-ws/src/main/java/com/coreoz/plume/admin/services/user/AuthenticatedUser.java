package com.coreoz.plume.admin.services.user;

import java.util.Set;

import com.coreoz.plume.admin.db.generated.AdminUser;

public interface AuthenticatedUser {

	AdminUser getUser();
	Set<String> getPermissions();

}
