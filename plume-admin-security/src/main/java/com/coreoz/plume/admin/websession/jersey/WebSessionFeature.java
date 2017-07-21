package com.coreoz.plume.admin.websession.jersey;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.websession.WebSessionPermission;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.jersey.security.permission.PermissionFeature;

public class WebSessionFeature<T extends WebSessionPermission, A extends Annotation>
	implements DynamicFeature {

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
