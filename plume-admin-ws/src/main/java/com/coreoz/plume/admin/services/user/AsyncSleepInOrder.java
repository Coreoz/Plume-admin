package com.coreoz.plume.admin.services.user;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide async sleep feature.
 *
 * Works correctly only in a context when all sleeping calls end after each other.
 * So for example:
 * - Will work as expected when called in this order at the same time:
 * 	 - sleepAsync(5), sleepAsync(6), sleepAsync(7), sleepAsync(8)
 *   - sleepAsync(5), sleepAsync(5), sleepAsync(5), sleepAsync(5)
 * - Will not work as expected when called in this order:
 *   - sleepAsync(80), sleepAsync(10), sleepAsync(10), sleepAsync(10) <-- in that case, the 4 calls will resolve more or less at the same time, after 80ms
 */
public class AsyncSleepInOrder {
	private static final Logger logger = LoggerFactory.getLogger(AsyncSleepInOrder.class);

	private static final Thread executor = new Thread(AsyncSleepInOrder::sleepUntilNextWakingUpTime);

	private static final LinkedBlockingQueue<SleepingTask> sleepingTasks = new LinkedBlockingQueue<>();

	static {
		executor.start();
	}

	/**
	 * Async {@link Thread#sleep}
	 * @param timeToSleepInMillis
	 * @return The CompletableFuture the will resolve after the delay {@code timeToSleepInMillis} has passed
	 */
	public static CompletableFuture<Void> sleepAsync(long timeToSleepInMillis) {
		CompletableFuture<Void> promise = new CompletableFuture<>();
		sleepingTasks.add(new SleepingTask(System.currentTimeMillis() + timeToSleepInMillis, promise));
		return promise;
	}

	private static void sleepUntilNextWakingUpTime() {
		while (true) {
			try {
				SleepingTask task = sleepingTasks.take();
				try {
					long timeToSleep = task.timeWhenToWakeUp - System.currentTimeMillis();
					if (timeToSleep > 0) {
						Thread.sleep(timeToSleep);
					}
				} finally {
					task.promise.complete(null);
				}
			} catch (InterruptedException e) {
				logger.error("SleepingTask has been interrupted", e);
			}
		}
	}

	private static class SleepingTask {
		long timeWhenToWakeUp;
		CompletableFuture<Void> promise;
		SleepingTask(long timeWhenToWakeUp, CompletableFuture<Void> promise) {
			this.timeWhenToWakeUp = timeWhenToWakeUp;
			this.promise = promise;
		}
	}
}
