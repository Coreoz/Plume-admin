package com.coreoz.plume.admin.jersey;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.jersey.security.permission.PermissionFeature;

public class WebSessionFeature<T extends WebSessionPermission, A extends Annotation>
	implements DynamicFeature {

	static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";

	static final String BEARER_PREFIX = "Bearer ";
	static final Object EMPTY_SESSION = new Object();

	private final PermissionFeature<A> permissionFeature;

	public WebSessionFeature(WebSessionSigner webSessionSigner, Class<T> webSessionClass,
			Class<A> permissionAnnotationType,
			Function<A, String> permissionAnnotationExtractor) {
		this.permissionFeature = new PermissionFeature<>(
			new WebSessionRequestPermissionProvider<>(
				webSessionSigner,
				webSessionClass
			),
			permissionAnnotationType,
			permissionAnnotationExtractor
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		permissionFeature.configure(resourceInfo, context);
	}

}
