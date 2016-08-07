package com.coreoz.plume.admin.services.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeProvider {

	/**
	 * Returns the current time in milliseconds
	 */
	long currentTime();

	LocalDate currentLocalDate();

	LocalDateTime currentDateTime();

}
