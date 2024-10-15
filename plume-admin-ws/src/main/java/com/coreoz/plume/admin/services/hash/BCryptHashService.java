package com.coreoz.plume.admin.services.hash;

import jakarta.inject.Singleton;

import org.mindrot.jbcrypt.BCrypt;

@Singleton
public class BCryptHashService implements HashService {

	private static final int BCRYPT_SALT_ROUND = 11;

	@Override
	public String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
		return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_SALT_ROUND));
	}

	@Override
	public boolean checkPassword(String candidate, String hashed) {
		return BCrypt.checkpw(candidate, hashed);
	}

}
