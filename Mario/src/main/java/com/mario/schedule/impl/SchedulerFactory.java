package com.mario.schedule.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mario.schedule.Scheduler;

public final class SchedulerFactory {

	private final int scheduledExecutorServicePoolSize = Integer
			.valueOf(System.getProperty("application.scheduler.worker", "8"));
	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
			scheduledExecutorServicePoolSize, new ThreadFactoryBuilder().setNameFormat("Scheduler Thread #%d").build());

	private static final SchedulerFactory instance = new SchedulerFactory();

	public static SchedulerFactory getInstance() {
		return instance;
	}

	private SchedulerFactory() {
		// do nothing
	}

	public Scheduler newSchedulerInstance() {
		return new SchedulerImpl(scheduledExecutorService);
	}

	public void stop() {
		ScheduledEventExecutor.getInstance().stop();
		this.scheduledExecutorService.shutdown();
		try {
			if (this.scheduledExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
				this.scheduledExecutorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
