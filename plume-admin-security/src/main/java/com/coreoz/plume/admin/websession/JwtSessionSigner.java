package com.coreoz.plume.admin.websession;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coreoz.plume.services.time.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * A generic class to help serialialize/deserialialize objets from/to JWT.
 * Note that to use this class, you must have <code>plume-services</code> in your classpath.
 */
public class JwtSessionSigner implements WebSessionSigner {

	private static final Logger logger = LoggerFactory.getLogger(JwtSessionSigner.class);

	private final Key signingKey;
	private final SignatureAlgorithm signatureAlgorithm;

	private final ObjectMapper objectMapper;

	private final JwtParser jwtParser;

	public JwtSessionSigner(String jwtSecret,
			ObjectMapper objectMapper, TimeProvider timeProvider) {
		this.signatureAlgorithm = SignatureAlgorithm.HS384;
		this.signingKey = new SecretKeySpec(
			jwtSecret.getBytes(),
			signatureAlgorithm.getJcaName()
		);
		this.objectMapper = objectMapper;
		this.jwtParser = Jwts
			.parserBuilder()
			.setSigningKey(signingKey)
			.setClock(() -> new Date(timeProvider.currentTime()))
			.build();
	}

	/**
	 * Returns an instance of {@link #T} if the session could be read and is fully valid
	 * or null otherwise.
	 */
	@Override
	public <T> T parseSession(String webSesionSerialized, Class<T> sessionClass) {
		try {
			Claims sessionAsMap = jwtParser
				.parseClaimsJws(webSesionSerialized)
				.getBody();
			return objectMapper.convertValue(sessionAsMap, sessionClass);
		} catch (ExpiredJwtException e) {
			logger.warn(e.getMessage());
		} catch (Exception e) {
			logger.warn("Cannot read the web session", e);
		}
		return null;
	}

	/**
	 * Serialize into a string the session and sign it
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String serializeSession(Object sessionInformation, Long expirationTime) {
		JwtBuilder jwtBuilder = Jwts
			.builder()
			.signWith(signingKey, signatureAlgorithm)
			.setClaims(objectMapper.convertValue(sessionInformation, Map.class));

		if(expirationTime != null) {
			jwtBuilder.setExpiration(new Date(expirationTime));
		}

		return jwtBuilder.compact();
	}

}
