package com.coreoz.plume.admin.services.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.inject.Singleton;

@Singleton
public class SystemTimeProvider implements TimeProvider {

	@Override
	public long currentTime() {
		return System.currentTimeMillis();
	}

	@Override
	public LocalDate currentLocalDate() {
		return LocalDate.now();
	}

	@Override
	public LocalDateTime currentDateTime() {
		return LocalDateTime.now();
	}

}
