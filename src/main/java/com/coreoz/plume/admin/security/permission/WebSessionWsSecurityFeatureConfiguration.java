package com.coreoz.plume.admin.security.permission;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import com.coreoz.plume.admin.services.time.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder
public class WebSessionWsSecurityFeatureConfiguration<T extends WebSessionPermission, A extends Annotation> {

	private final String requestAttributeName;
	private final String httpHeaderName;
	private final String jwtSecret;
	private final Class<T> webSessionClass;
	private final Class<A> permissionAnnotationType;
	private final Function<A, String> permissionAnnotationExtractor;
	private final ObjectMapper objectMapper;
	private final TimeProvider timeProvider;

	public WebSessionWsSecurityFeatureConfiguration(String requestAttributeName,
			String httpHeaderName, String jwtSecret, Class<T> webSessionClass,
			Class<A> permissionAnnotationType, Function<A, String> permissionAnnotationExtractor,
			ObjectMapper objectMapper, TimeProvider timeProvider) {
		this.requestAttributeName = Preconditions.checkNotNull(requestAttributeName);
		this.httpHeaderName = Preconditions.checkNotNull(httpHeaderName);
		this.jwtSecret = Preconditions.checkNotNull(jwtSecret);
		this.webSessionClass = Preconditions.checkNotNull(webSessionClass);
		this.permissionAnnotationType = Preconditions.checkNotNull(permissionAnnotationType);
		this.permissionAnnotationExtractor = Preconditions.checkNotNull(permissionAnnotationExtractor);
		this.objectMapper = Preconditions.checkNotNull(objectMapper);
		this.timeProvider = Preconditions.checkNotNull(timeProvider);
	}

}
