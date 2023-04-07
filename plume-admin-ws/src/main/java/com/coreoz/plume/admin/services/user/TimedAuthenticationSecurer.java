package com.coreoz.plume.admin.services.user;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.admin.services.hash.HashService;
import com.coreoz.plume.services.time.TimeProvider;

/**
 * Provide a way to protect against timing attacks
 */
@Singleton
public class TimedAuthenticationSecurer {

	private static final String INITIAL_PASSWORD_FOR_TIME_CALIBRATION = "password";
	private static final long UNSET_LAST_DURATION = -1L;

	private final HashService hashService;
	private final TimeProvider timeProvider;
	private final ExecutorService executorService;
	private long lastDurationInMillis;

	@Inject
	public TimedAuthenticationSecurer(HashService hashService, TimeProvider timeProvider) {
		this(hashService, timeProvider, 1);
	}

	public TimedAuthenticationSecurer(HashService hashService, TimeProvider timeProvider, int maxThreadsUsed) {
		this.hashService = hashService;
		this.timeProvider = timeProvider;
		this.executorService = Executors.newFixedThreadPool(maxThreadsUsed);
		this.lastDurationInMillis = UNSET_LAST_DURATION;
	}

	public<U> CompletableFuture<Optional<U>> verifyPasswordAuthentication(
		Optional<U> user,
		Function<U, String> hashedPasswordExtractor,
		String passwordCandidate
	) {
		CompletableFuture<Optional<U>> verifyPasswordPromise = verifyPassword(user, hashedPasswordExtractor, passwordCandidate);
		return CompletableFuture
			.allOf(verifyPasswordPromise, waitRandomly(user.isPresent()))
			.thenApply(voidResult -> verifyPasswordPromise.getNow(Optional.empty()));
	}

	public void initialize() {
		waitRandomly(false);
	}

	private<U> CompletableFuture<Optional<U>> verifyPassword(
		Optional<U> user,
		Function<U, String> hashedPasswordExtractor,
		String passwordCandidate
	) {
		if (user.isEmpty()) {
			// no user, no verification to be made
			return CompletableFuture.completedFuture(Optional.empty());
		}
		U presentUser = user.get();
		return passwordVerification(
			hashedPasswordExtractor.apply(presentUser),
			passwordCandidate
		).thenApply(passwordValidationResult ->	Boolean.TRUE.equals(passwordValidationResult) ? Optional.of(presentUser) : Optional.empty());
	}

	private CompletableFuture<Void> waitRandomly(boolean isUserFound) {
		if (lastDurationInMillis == UNSET_LAST_DURATION) {
			if (isUserFound) {
				// If it is the first connection and there is a user found,
				// then it means the hashing function will be called.
				// So there is no need to add an extra delay to calibrate
				// the hashing function duration
				return CompletableFuture.completedFuture(null);
			}
			String hashedSeedPassword = hashService.hashPassword(INITIAL_PASSWORD_FOR_TIME_CALIBRATION);
			return passwordVerification(hashedSeedPassword, INITIAL_PASSWORD_FOR_TIME_CALIBRATION)
				.thenApply(unusedValue -> null);
		}
		return AsyncSleepInOrder.sleepAsync(lastDurationInMillis);
	}

	private CompletableFuture<Boolean> passwordVerification(String hashedPassword, String candidatePassword) {
		return CompletableFuture.supplyAsync(
			() -> timedPasswordVerification(hashedPassword, candidatePassword),
			executorService
		);
	}

	private boolean timedPasswordVerification(String hashedPassword, String candidatePassword) {
		long currentTimeInMillis = timeProvider.currentTime();
		boolean hasVerificationPassed = hashService.checkPassword(candidatePassword, hashedPassword);
		lastDurationInMillis = timeProvider.currentTime() - currentTimeInMillis;
		return hasVerificationPassed;
	}
}
