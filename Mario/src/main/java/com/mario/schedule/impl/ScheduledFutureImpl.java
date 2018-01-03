package com.mario.schedule.impl;

import com.mario.schedule.ScheduledFuture;

class ScheduledFutureImpl implements ScheduledFuture {

	private static long idSeed = 0;

	private java.util.concurrent.ScheduledFuture<?> future;
	private long delay;
	private boolean cancelled = false;

	private static long getNextId() {
		return ++idSeed;
	};

	private long startTimeMillis;
	private long id = getNextId();

	public ScheduledFutureImpl(long delay, java.util.concurrent.ScheduledFuture<?> future) {
		this.delay = delay;
		this.future = future;
	}

	public ScheduledFutureImpl(long delay) {
		this.delay = delay;
	}

	void updateStartTime() {
		this.startTimeMillis = System.currentTimeMillis();
	}

	@Override
	public long getId() {
		return this.id;
	}

	@Override
	public void cancel() {
		this.cancelled = true;
		if (future != null) {
			this.future.cancel(true);
			this.future = null;
		}
	}

	@Override
	public long getRemainingTimeToNextOccurrence() {
		return this.delay - (System.currentTimeMillis() - this.startTimeMillis);
	}

	@Override
	public long getStartTime() {
		return this.startTimeMillis;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	void setFuture(java.util.concurrent.ScheduledFuture<?> future) {
		this.future = future;
	}

	void setDelay(long delay) {
		this.delay = delay;
	}
}
