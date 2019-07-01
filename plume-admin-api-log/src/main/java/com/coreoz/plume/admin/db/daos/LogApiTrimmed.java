package com.coreoz.plume.admin.db.daos;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Value;

@Value
public class LogApiTrimmed {
	@JsonSerialize(using=ToStringSerializer.class)
	private final Long id;
	private final Instant date;
	private final String method;
	private final String api;
	private final String url;
	private final String statusCode;
}
