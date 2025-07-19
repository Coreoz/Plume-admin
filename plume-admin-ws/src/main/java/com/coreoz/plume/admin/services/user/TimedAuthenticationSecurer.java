package com.coreoz.plume.admin.services.user;

import com.coreoz.plume.admin.services.configuration.AdminTimingSecurityConfigurationService;
import com.coreoz.plume.admin.services.hash.HashService;
import com.google.common.collect.EvictingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Provide a way to protect against timing attacks
 */
@Singleton
public class TimedAuthenticationSecurer {

    private static final String INITIAL_PASSWORD_FOR_TIME_CALIBRATION = "password";

    private final HashService hashService;
    private final Clock clock;
    private final AdminTimingSecurityConfigurationService configurationService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final EvictingQueue<Long> lastDurations;

    @Inject
    public TimedAuthenticationSecurer(HashService hashService, Clock clock, AdminTimingSecurityConfigurationService configurationService) {
        this.hashService = hashService;
        this.clock = clock;
        this.configurationService = configurationService;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(configurationService.threadPoolSize());
        this.lastDurations = EvictingQueue.create(configurationService.movingAverageWindow());
    }

    public <U> CompletableFuture<Optional<U>> verifyPasswordAuthentication(
        Optional<U> user,
        Function<U, String> hashedPasswordExtractor,
        String passwordCandidate
    ) {
        if (!configurationService.enabled()) {
            return verifyPassword(user, hashedPasswordExtractor, passwordCandidate);
        }

        CompletableFuture<Optional<U>> verifyPasswordPromise = verifyPassword(user, hashedPasswordExtractor, passwordCandidate);
        return CompletableFuture
            .allOf(verifyPasswordPromise, waitRandomly(user.isPresent()))
            .thenApply(voidResult -> verifyPasswordPromise.getNow(Optional.empty()));
    }

    // FIXME who calls this?
    public void initialize() {
        if (configurationService.enabled()) {
            waitRandomly(false);
        }
    }

    private <U> CompletableFuture<Optional<U>> verifyPassword(
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
        ).thenApply(passwordValidationResult -> Boolean.TRUE.equals(passwordValidationResult) ? Optional.of(presentUser) : Optional.empty());
    }

    private CompletableFuture<Void> waitRandomly(boolean isUserFound) {
        if (lastDurations.remainingCapacity() > 0) {
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
        CompletableFuture<Void> promise = new CompletableFuture<>();
        // TODO how can we change it so malicious users cannot perform DoS attacks using wrong password?
        scheduledExecutorService.schedule(() -> promise.complete(null), computeDelay(), TimeUnit.MILLISECONDS);
        return promise;
    }

    private long computeDelay() {
        return (long) lastDurations.stream().mapToLong(l -> l).average().orElse(0);
    }

    private CompletableFuture<Boolean> passwordVerification(String hashedPassword, String candidatePassword) {
        return CompletableFuture.supplyAsync(
            () -> timedPasswordVerification(hashedPassword, candidatePassword),
            scheduledExecutorService
        );
    }

    private boolean timedPasswordVerification(String hashedPassword, String candidatePassword) {
        long currentTimeInMillis = clock.millis();
        boolean hasVerificationPassed = hashService.checkPassword(candidatePassword, hashedPassword);
        lastDurations.add(clock.millis() - currentTimeInMillis);
        return hasVerificationPassed;
    }
}
