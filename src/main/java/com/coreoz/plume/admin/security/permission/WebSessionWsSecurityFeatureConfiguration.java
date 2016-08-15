package com.coreoz.plume.admin.security.permission;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder
public class WebSessionWsSecurityFeatureConfiguration<A extends Annotation> {

	private final String requestAttributeName;
	private final Class<? extends WebSessionPermission> webSessionClass;
	private final Class<A> permissionAnnotationType;
	private final Function<A, String> permissionAnnotationExtractor;
	private final WebSessionSigner webSessionSigner;

	public WebSessionWsSecurityFeatureConfiguration(String requestAttributeName,
			Class<? extends WebSessionPermission> webSessionClass,
			Class<A> permissionAnnotationType, Function<A, String> permissionAnnotationExtractor,
			WebSessionSigner webSessionSigner) {
		this.requestAttributeName = Preconditions.checkNotNull(requestAttributeName);
		this.webSessionClass = Preconditions.checkNotNull(webSessionClass);
		this.permissionAnnotationType = Preconditions.checkNotNull(permissionAnnotationType);
		this.permissionAnnotationExtractor = Preconditions.checkNotNull(permissionAnnotationExtractor);
		this.webSessionSigner = webSessionSigner;
	}

}
