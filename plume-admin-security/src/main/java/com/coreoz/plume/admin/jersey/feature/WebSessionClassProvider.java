package com.coreoz.plume.admin.jersey.feature;

import com.coreoz.plume.admin.websession.WebSessionPermission;

public interface WebSessionClassProvider {

	Class<? extends WebSessionPermission> webSessionClass();

}
