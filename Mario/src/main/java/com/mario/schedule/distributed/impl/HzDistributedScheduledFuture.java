package com.mario.schedule.distributed.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.scheduledexecutor.IScheduledFuture;
import com.mario.schedule.distributed.DistributedScheduledFuture;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

public class HzDistributedScheduledFuture extends HzDistributedScheduleCleaner implements DistributedScheduledFuture {

	@Getter
	private final IScheduledFuture<?> sourceFuture;

	private long startTime;

	public HzDistributedScheduledFuture(String taskName, IScheduledFuture<?> sourceFuture, String trackingMapName) {
		this(taskName, sourceFuture, trackingMapName, null);
	}

	public HzDistributedScheduledFuture(String taskName, IScheduledFuture<?> sourceFuture, String trackingMapName,
			HazelcastInstance hazelcastInstance) {
		super(taskName, trackingMapName, hazelcastInstance);
		if (sourceFuture == null) {
			throw new NullPointerException("source future cannot be null");
		}
		this.sourceFuture = sourceFuture;
		this.startTime = System.currentTimeMillis();
	}

	void autoSyncStartTime() {
		Long currValue = getStartTimeTrackingMap().putIfAbsent(this.getTaskName(), this.startTime);
		if (currValue != null) {
			this.startTime = currValue;
		}
	}

	@Override
	public void cancel() {
		this.sourceFuture.cancel(false);
		this.cleanTask();
	}

	@Override
	public void cancelNow() {
		this.sourceFuture.cancel(true);
		this.cleanTask();
	}

	@Override
	public long getElaspedTime(TimeUnit timeUnit) {
		if (timeUnit == null) {
			throw new IllegalArgumentException("TimeUnit must be not null");
		}

		long timeMillis = System.currentTimeMillis() - this.startTime;
		long result = -1;
		switch (timeUnit) {
		case NANOSECONDS:
			result = timeMillis * (long) 1e6;
			break;
		case MICROSECONDS:
			result = timeMillis * (long) 1e3;
			break;
		case MILLISECONDS:
			result = timeMillis;
			break;
		case SECONDS:
			result = timeMillis / (long) 1e3;
			break;
		case MINUTES:
			result = timeMillis / (long) 1e3 / 60;
			break;
		case HOURS:
			result = timeMillis / (long) 1e3 / 3600;
			break;
		case DAYS:
			result = timeMillis / (long) 1e3 / 3600 / 24;
			break;
		}
		return result;
	}

	@Override
	public long getTotalRuns() {
		return this.sourceFuture.getStats().getTotalRuns();
	}

	@Override
	public long getTotalRunTime(TimeUnit timeUnit) {
		return this.sourceFuture.getStats().getTotalRunTime(timeUnit);
	}

}
