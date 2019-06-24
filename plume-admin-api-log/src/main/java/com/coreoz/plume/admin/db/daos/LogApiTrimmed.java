package com.coreoz.plume.admin.db.daos;

import lombok.Value;

@Value
public class LogApiTrimmed {
	private final Long id;
	private final String method;
	private final String api;
	private final String url;
	private final String statusCode;
}
