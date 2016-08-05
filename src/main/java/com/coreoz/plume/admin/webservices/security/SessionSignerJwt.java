package com.coreoz.plume.admin.webservices.security;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.coreoz.plume.admin.services.time.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

@Singleton
public class SessionSignerJwt implements SessionSigner {

	private static final Logger logger = LoggerFactory.getLogger(SessionSignerJwt.class);

	private final JWTVerifier jwtVerifier;
	private final JWTSigner jwtSigner;

	private final ObjectMapper objectMapper;
	private final TimeProvider timeProvider;

	@Inject
	public SessionSignerJwt(Config config, ObjectMapper objectMapper, TimeProvider timeProvider) {
		String jwtSecret = config.getString("admin.jwt-secret");

		this.jwtVerifier = new JWTVerifier(jwtSecret);
		this.jwtSigner = new JWTSigner(jwtSecret);

		this.objectMapper = objectMapper;
		this.timeProvider = timeProvider;
	}

	@Override
	public <T extends SessionInformation> T parseSession(String sessionInformationSerialized, Class<T> sessionInformationClass) {
		try {
			Map<String, Object> sessionAsMap = jwtVerifier.verify(sessionInformationSerialized);
			T expiringInformation = objectMapper.convertValue(sessionAsMap, sessionInformationClass);
			if (expiringInformation.getExpirationTime()<timeProvider.currentTime()) {
				return null;
			}
			return expiringInformation;
		} catch (Exception e) {
			logger.warn("", e);
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends SessionInformation> String serializeSession(T sessionInformation) {
		return jwtSigner.sign(objectMapper.convertValue(sessionInformation, Map.class));
	}

}
