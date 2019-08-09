package com.coreoz.plume.admin.webservices.data.session;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AdminSession {

	private final String webSessionToken;
	private final long refreshDurationInMillis;
	private final long inactiveDurationInMillis;

}
