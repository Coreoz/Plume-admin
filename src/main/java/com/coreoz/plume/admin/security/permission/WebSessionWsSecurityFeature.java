package com.coreoz.plume.admin.security.permission;

import java.lang.annotation.Annotation;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.websession.WebSessionRequestFetcher;
import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.admin.websession.WebSessionSignerJwt;
import com.coreoz.plume.jersey.security.WsPermissionAuthenticator;
import com.coreoz.plume.jersey.security.WsSecurityFeature;

public class WebSessionWsSecurityFeature<T extends WebSessionPermission, A extends Annotation> implements DynamicFeature {

	private final WsSecurityFeature<A> wsSecurityFeature;

	public WebSessionWsSecurityFeature(WebSessionWsSecurityFeatureConfiguration<T, A> conf) {
		WebSessionSigner<T> webSessionSigner = new WebSessionSignerJwt<>(
			conf.webSessionClass(),
			conf.jwtSecret(),
			conf.objectMapper(),
			conf.timeProvider()
		);

		WebSessionRequestFetcher<T> webSessionRequestFetcher = new WebSessionRequestFetcher<>(
			webSessionSigner,
			request -> request.getHeaderString(conf.httpHeaderName()),
			conf.requestAttributeName()
		);

		this.wsSecurityFeature = new WsSecurityFeature<>(
			new WsPermissionAuthenticator(
				new WsRequestPermissionProviderWebSession<>(webSessionRequestFetcher)
			),
			conf.permissionAnnotationType(),
			conf.permissionAnnotationExtractor()
		);
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		wsSecurityFeature.configure(resourceInfo, context);
	}

}
