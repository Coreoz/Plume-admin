package com.coreoz.plume.admin.services.time;

import javax.inject.Singleton;

@Singleton
public class SystemTimeProvider implements TimeProvider {

	@Override
	public long currentTime() {
		return System.currentTimeMillis();
	}

}
