package com.coreoz.plume.admin.websession;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.util.Date;
import java.util.Map;

/**
 * A generic class to help serialialize/deserialialize objets from/to JWT.
 * Note that to use this class, you must have <code>plume-services</code> in your classpath.
 */
public class JwtSessionSigner implements WebSessionSigner {
	private static final Logger logger = LoggerFactory.getLogger(JwtSessionSigner.class);

	private final SecretKey signingKey;

	private final ObjectMapper objectMapper;

	private final JwtParser jwtParser;

	public JwtSessionSigner(String jwtSecret,
			ObjectMapper objectMapper, Clock clock) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
		this.objectMapper = objectMapper;
		this.jwtParser = Jwts
			.parser()
			.verifyWith(signingKey)
			.clock(() -> new Date(clock.millis()))
			.build();
	}

	/**
	 * Returns an instance of <code>T</code> if the session could be read and is fully valid
	 * or null otherwise.
	 */
	@Override
	public <T> T parseSession(String webSessionSerialized, Class<T> sessionClass) {
		try {
			Claims sessionAsMap = jwtParser
				.parseSignedClaims(webSessionSerialized)
				.getPayload();
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
			.signWith(signingKey)
			.claims(objectMapper.convertValue(sessionInformation, Map.class));

		if(expirationTime != null) {
			jwtBuilder.expiration(new Date(expirationTime));
		}

		return jwtBuilder.compact();
	}
}
