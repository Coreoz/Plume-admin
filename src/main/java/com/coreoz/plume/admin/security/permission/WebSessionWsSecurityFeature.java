package com.coreoz.plume.admin.security.permission;

import java.lang.annotation.Annotation;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.websession.WebSessionRequestFetcher;
import com.coreoz.plume.jersey.security.WsPermissionAuthenticator;
import com.coreoz.plume.jersey.security.WsSecurityFeature;
import com.google.common.net.HttpHeaders;

public class WebSessionWsSecurityFeature<A extends Annotation> implements DynamicFeature {

	private static final String AUTHORIZATION_BEARER = "Bearer ";

	private final WsSecurityFeature<A> wsSecurityFeature;

	public WebSessionWsSecurityFeature(WebSessionWsSecurityFeatureConfiguration<A> conf) {
		WebSessionRequestFetcher<? extends WebSessionPermission> webSessionRequestFetcher = new WebSessionRequestFetcher<>(
			conf.webSessionSigner(),
			conf.webSessionClass(),
			WebSessionWsSecurityFeature::authorizationBearer,
			conf.requestAttributeName()
		);

		this.wsSecurityFeature = new WsSecurityFeature<>(
			new WsPermissionAuthenticator(
				new WsRequestPermissionProviderWebSession(webSessionRequestFetcher)
			),
			conf.permissionAnnotationType(),
			conf.permissionAnnotationExtractor()
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		wsSecurityFeature.configure(resourceInfo, context);
	}

	public static String authorizationBearer(ContainerRequestContext request) {
		String authorization = request.getHeaderString(HttpHeaders.AUTHORIZATION);
		if(authorization == null || !authorization.startsWith(AUTHORIZATION_BEARER)) {
			return null;
		}
		return authorization.substring(AUTHORIZATION_BEARER.length());
	}


}
