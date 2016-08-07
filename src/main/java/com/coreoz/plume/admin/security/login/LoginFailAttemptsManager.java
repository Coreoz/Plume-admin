package com.coreoz.plume.admin.security.login;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.SneakyThrows;

/**
 * Handles failed login attempts
 */
public class LoginFailAttemptsManager {

	private final Cache<String, AtomicInteger> failedAttemps;
	private final int loginMaxAttempts;

	public LoginFailAttemptsManager(int loginMaxAttempts, Duration loginBlockedDuration) {
		this.loginMaxAttempts = loginMaxAttempts;

		this.failedAttemps = CacheBuilder
				.newBuilder()
				.expireAfterWrite(loginBlockedDuration.getSeconds(), TimeUnit.SECONDS)
				.build();
	}

	public boolean isBlocked(String username) {
		AtomicInteger nbAttempts = failedAttemps.getIfPresent(username);
		return nbAttempts != null && nbAttempts.get() >= loginMaxAttempts;
	}

	@SneakyThrows
	public void addAttempt(String username) {
		failedAttemps.get(username, () -> new AtomicInteger(0)).incrementAndGet();
	}

}
