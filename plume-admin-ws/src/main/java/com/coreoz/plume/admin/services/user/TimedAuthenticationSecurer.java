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
 *
 */
@Singleton
public class TimedAuthenticationSecurer {

	private static final String INITIAL_PASSWORD_SEED = "password";
	private static final long UNSET_LAST_DURATION = -1L;
	private static final Void VOID_FUTURE = (Void) new Object();

	private final HashService hashService;
	private final TimeProvider timeProvider;
	private final ExecutorService executorService;
	// TODO soit on ajoute directement ça au scheduler, soit il existe un autre moyen simple de faire ça
	// private final Scheduler scheduler;
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

	public<USER> CompletableFuture<Optional<USER>> verifyPasswordAuthentication(
		Optional<USER> user,
		Function<USER, String> hashedPasswordExtractor,
		String passwordCandidate
	) {
		CompletableFuture<Optional<USER>> verifyPasswordPromise = verifyPassword(user, hashedPasswordExtractor, passwordCandidate);
		return CompletableFuture
			.allOf(verifyPasswordPromise, waitRandomly(user.isPresent()))
			.thenApply((voidResult) -> verifyPasswordPromise.getNow(Optional.empty()));
	}

	private<USER> CompletableFuture<Optional<USER>> verifyPassword(
		Optional<USER> user,
		Function<USER, String> hashedPasswordExtractor,
		String passwordCandidate
	) {
		if (user.isEmpty()) {
			// no user, no verification to be made
			return CompletableFuture.completedFuture(Optional.empty());
		}
		USER presentUser = user.get();
		return passwordVerification(
			hashedPasswordExtractor.apply(presentUser),
			passwordCandidate
		).thenApply((passwordValidationResult) -> {
			return passwordValidationResult ? Optional.of(presentUser) : Optional.empty();
		});
	}

	private CompletableFuture<Void> waitRandomly(boolean isUserFound) {
		if (lastDurationInMillis == UNSET_LAST_DURATION) {
			if (isUserFound) {
				// If it is the first connection and there is a user found,
				// then it means the hashing function will be called.
				// So there is no need to add an extra delay to calibrate
				// the hashing function duration
				return CompletableFuture.completedFuture(VOID_FUTURE);
			}
			String hashedSeedPassword = hashService.hashPassword(INITIAL_PASSWORD_SEED);
			return passwordVerification(hashedSeedPassword, INITIAL_PASSWORD_SEED)
				.thenApply((unusedValue) -> VOID_FUTURE);
		}
		// TODO surement passer par Wisp ou autre pour notifier la fin de l'attente
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
