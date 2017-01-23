package com.coreoz.plume.admin.websession;

import java.security.Key;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coreoz.plume.admin.services.configuration.AdminConfigurationService;
import com.coreoz.plume.services.time.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Singleton
public class WebSessionSigner {

	private static final Logger logger = LoggerFactory.getLogger(WebSessionSigner.class);

	private final Key signingKey;
	private final SignatureAlgorithm signatureAlgorithm;

	private final ObjectMapper objectMapper;
	private final TimeProvider timeProvider;

	@Inject
	public WebSessionSigner(AdminConfigurationService conf,
			ObjectMapper objectMapper, TimeProvider timeProvider) {
		this.signatureAlgorithm = SignatureAlgorithm.HS512;
		this.signingKey = new SecretKeySpec(
			conf.jwtSecret().getBytes(),
			signatureAlgorithm.getJcaName()
		);
		this.objectMapper = objectMapper;
		this.timeProvider = timeProvider;
	}

	/**
	 * Returns an instance of {@link #T} if the session could be read and is fully valid
	 * or null otherwise.
	 */
	public <T extends WebSession> T parseSession(String webSesionSerialized, Class<T> sessionClass) {
		try {
			Claims sessionAsMap = Jwts
				.parser()
				.setSigningKey(signingKey)
				.parseClaimsJws(webSesionSerialized)
				.getBody();
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

	/**
	 * Serialize into a string the session and sign it
	 */
	@SuppressWarnings("unchecked")
	public String serializeSession(WebSession sessionInformation) {
		return Jwts
			.builder()
			.signWith(signatureAlgorithm, signingKey)
			.setClaims(objectMapper.convertValue(sessionInformation, Map.class))
			.compact();
	}

}
