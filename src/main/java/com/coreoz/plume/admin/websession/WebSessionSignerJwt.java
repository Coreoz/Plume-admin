package com.coreoz.plume.admin.websession;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.services.time.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class WebSessionSignerJwt implements WebSessionSigner {

	private static final Logger logger = LoggerFactory.getLogger(WebSessionSignerJwt.class);

	private final JWTVerifier jwtVerifier;
	private final JWTSigner jwtSigner;

	private final ObjectMapper objectMapper;
	private final TimeProvider timeProvider;

	@Inject
	public WebSessionSignerJwt(AdminConfigurationService conf,
			ObjectMapper objectMapper, TimeProvider timeProvider) {
		this.jwtVerifier = new JWTVerifier(conf.jwtSecret());
		this.jwtSigner = new JWTSigner(conf.jwtSecret());

		this.objectMapper = objectMapper;
		this.timeProvider = timeProvider;
	}

	@Override
	public <T extends WebSession> T parseSession(String webSesionSerialized, Class<T> sessionClass) {
		try {
			Map<String, Object> sessionAsMap = jwtVerifier.verify(webSesionSerialized);
			T expiringInformation = objectMapper.convertValue(sessionAsMap, sessionClass);
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
	public String serializeSession(Object sessionInformation) {
		return jwtSigner.sign(objectMapper.convertValue(sessionInformation, Map.class));
	}

}
