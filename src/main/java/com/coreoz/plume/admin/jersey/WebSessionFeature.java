package com.coreoz.plume.admin.jersey;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.function.Function;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import com.coreoz.plume.admin.websession.WebSessionSigner;
import com.coreoz.plume.jersey.security.permission.PermissionFeature;
import com.coreoz.plume.jersey.security.permission.PermissionRequestProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;

public class WebSessionFeature<T extends WebSessionPermission, A extends Annotation>
	implements DynamicFeature {

	public static final String REQUEST_SESSION_ATTRIBUTE_NAME = "sessionInfo";

	private static final String BEARER_PREFIX = "Bearer ";
	private static final Object EMPTY_SESSION = new Object();

	private final PermissionFeature<A> permissionFeature;

	public WebSessionFeature(WebSessionSigner webSessionSigner, Class<T> webSessionClass,
			Class<A> permissionAnnotationType,
			Function<A, String> permissionAnnotationExtractor) {
		this.permissionFeature = new PermissionFeature<>(
			new RequestPermissionProviderWebSession<>(
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

	private static class RequestPermissionProviderWebSession<T extends WebSessionPermission> implements PermissionRequestProvider {

		private final WebSessionSigner webSessionSigner;
		private final Class<T> webSessionClass;

		RequestPermissionProviderWebSession(
				WebSessionSigner webSessionSigner, Class<T> webSessionClass) {
			this.webSessionSigner = webSessionSigner;
			this.webSessionClass = webSessionClass;
		}

		@Override
		public String userInformation(ContainerRequestContext requestContext) {
			WebSessionPermission session = currentSessionInformation(requestContext);
			return session == null ? "No user connected" : session.getUserName();
		}

		@Override
		public Collection<String> correspondingPermissions(ContainerRequestContext requestContext) {
			WebSessionPermission session = currentSessionInformation(requestContext);
			return session == null ? ImmutableList.of() : session.getPermissions();
		}

		@SuppressWarnings("unchecked")
		private T currentSessionInformation(ContainerRequestContext request) {
			Object webSession = request.getProperty(REQUEST_SESSION_ATTRIBUTE_NAME);
			if(webSession == null) {
				String webSessionSerialized = parseAuthorizationBearer(request);
				webSession = webSessionSerialized == null ? null : webSessionSigner.parseSession(webSessionSerialized, webSessionClass);
				if(webSession == null) {
					webSession = EMPTY_SESSION;
				}
				request.setProperty(REQUEST_SESSION_ATTRIBUTE_NAME, webSession);
			}
			return webSession == EMPTY_SESSION ? null : (T) webSession;
		}

		private String parseAuthorizationBearer(ContainerRequestContext request) {
			String authorization = request.getHeaderString(HttpHeaders.AUTHORIZATION);
			if(authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
				return null;
			}
			return authorization.substring(BEARER_PREFIX.length());
		}

	}

}
