package com.coreoz.plume.admin.services.logApi;

import lombok.Value;

@Value
public class HttpBodyPart {

	private final String apiName;
	private final String body;
	private final String fileExtension;

}
