package com.coreoz.plume.admin.services.hash;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.inject.Singleton;

@Singleton
public class BCryptHashService implements HashService {
    private static final int BCRYPT_SALT_ROUND = 12;

	private static final BCrypt.Hasher hasher = BCrypt.withDefaults();
	private static final BCrypt.Verifyer verifyer = BCrypt.verifyer();

	@Override
	public String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        return hasher.hashToString(BCRYPT_SALT_ROUND, password.toCharArray());
	}

	@Override
	public boolean checkPassword(String candidate, String hashed) {
		return verifyer.verify(candidate.toCharArray(), hashed).verified;
	}

}
