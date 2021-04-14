package com.coreoz.plume.admin.services.logapi;

import lombok.Value;

@Value
public class HttpBodyPart {
	private final String apiName;
	private final String body;
	private final String fileExtension;
}
