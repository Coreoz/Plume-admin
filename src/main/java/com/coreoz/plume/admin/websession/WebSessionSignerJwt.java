package com.coreoz.plume.admin.websession;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.coreoz.plume.admin.services.time.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSessionSignerJwt<T extends WebSession> implements WebSessionSigner<T> {

	private static final Logger logger = LoggerFactory.getLogger(WebSessionSignerJwt.class);

	private final Class<T> webSessionClass;

	private final JWTVerifier jwtVerifier;
	private final JWTSigner jwtSigner;

	private final ObjectMapper objectMapper;
	private final TimeProvider timeProvider;

	public WebSessionSignerJwt(Class<T> webSessionClass, String jwtSecret, ObjectMapper objectMapper, TimeProvider timeProvider) {
		this.webSessionClass = webSessionClass;

		this.jwtVerifier = new JWTVerifier(jwtSecret);
		this.jwtSigner = new JWTSigner(jwtSecret);

		this.objectMapper = objectMapper;
		this.timeProvider = timeProvider;
	}

	@Override
	public T parseSession(String webSesionSerialized) {
		try {
			Map<String, Object> sessionAsMap = jwtVerifier.verify(webSesionSerialized);
			T expiringInformation = objectMapper.convertValue(sessionAsMap, webSessionClass);
			if (expiringInformation.getExpirationTime() < timeProvider.currentTime()) {
				return null;
			}
			return expiringInformation;
		} catch (Exception e) {
			logger.warn("Cannot read the web session", e);
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public String serializeSession(T sessionInformation) {
		return jwtSigner.sign(objectMapper.convertValue(sessionInformation, Map.class));
	}

}
