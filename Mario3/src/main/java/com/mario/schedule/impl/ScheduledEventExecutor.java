package com.mario.schedule.impl;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkerPool;
import com.mario.schedule.ScheduledCallback;
import com.nhb.common.BaseLoggable;

final class ScheduledEventExecutor extends BaseLoggable implements ExceptionHandler<ScheduledEvent> {

	private static final ScheduledEventExecutor instance = new ScheduledEventExecutor();

	static final ScheduledEventExecutor getInstance() {
		return instance;
	}

	private int numWorkers = 32;
	private WorkerPool<ScheduledEvent> workerPool;
	private RingBuffer<ScheduledEvent> ringBuffer;

	private ScheduledEventExecutor() {

		ScheduledWorker[] workers = new ScheduledWorker[numWorkers];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new ScheduledWorker();
		}

		this.workerPool = new WorkerPool<ScheduledEvent>(new EventFactory<ScheduledEvent>() {

			@Override
			public ScheduledEvent newInstance() {
				return new ScheduledEvent();
			}
		}, this, workers);

		this.ringBuffer = workerPool.start(new ThreadPoolExecutor(numWorkers, numWorkers, 6l, TimeUnit.SECONDS,
				new LinkedBlockingDeque<Runnable>(),
				new ThreadFactoryBuilder().setNameFormat("Scheduled Executor #%d").build()));
	}

	void publish(ScheduledCallback callback) {
		if (callback == null) {
			return;
		}
		long sequence = this.ringBuffer.next();
		try {
			ScheduledEvent event = this.ringBuffer.get(sequence);
			event.setCallback(callback);
		} finally {
			this.ringBuffer.publish(sequence);
		}
	}

	@Override
	public void handleEventException(Throwable ex, long sequence, ScheduledEvent event) {
		getLogger().error("handling scheduled event error", ex);
	}

	@Override
	public void handleOnStartException(Throwable ex) {
		getLogger().error("starting handling scheduled event error", ex);
	}

	@Override
	public void handleOnShutdownException(Throwable ex) {
		getLogger().error("shutting down scheduled event error", ex);
	}

	void stop() {
		if (this.workerPool.isRunning()) {
			try {
				this.workerPool.drainAndHalt();
			} catch (Exception ex) {
				System.err.println("stop schedule executor error");
				ex.printStackTrace();
			}
		}
	}

}
