package com.mario.schedule.distributed.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler;
import com.mario.schedule.distributed.DistributedScheduledFuture;
import com.mario.schedule.distributed.DistributedScheduler;
import com.mario.schedule.distributed.exception.DistributedScheduleException;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class HzDistributedScheduler implements DistributedScheduler, Serializable {

	private static final long serialVersionUID = -5883240048338160171L;

	private final HazelcastInstance hazelcast;
	private final IScheduledExecutorService hzScheduler;
	private final IMap<String, String> hzTrackingMap;

	public HzDistributedScheduler(String schedulerName, HazelcastInstance hazelcastInstance) {
		this.hazelcast = hazelcastInstance;
		this.hzScheduler = this.hazelcast.getScheduledExecutorService(schedulerName);
		this.hzTrackingMap = this.hazelcast.getMap(this.getName() + ":taskTracking");
	}

	@Override
	public String getName() {
		return this.hzScheduler.getName();
	}

	private IScheduledFuture<?> getSourceFutureFromUrn(String urn) {
		return this.hzScheduler.getScheduledFuture(ScheduledTaskHandler.of(urn));
	}

	private String getUrnFromName(String taskName) {
		return this.hzTrackingMap.get(taskName);
	}

	@Override
	public DistributedScheduledFuture getFutureByName(String taskName) {
		HzDistributedScheduledFuture result = new HzDistributedScheduledFuture(this.hzScheduler.getName(), taskName,
				this.getSourceFutureFromUrn(this.getUrnFromName(taskName)), this.hzTrackingMap.getName(),
				this.hazelcast);
		result.autoSyncStartTime();
		return result;
	}

	private DistributedScheduledFuture prepareFuture(String taskName, IScheduledFuture<?> future) {
		this.hzTrackingMap.put(taskName, future.getHandler().toUrn());
		HzDistributedScheduledFuture result = new HzDistributedScheduledFuture(this.hzScheduler.getName(), taskName,
				future, this.hzTrackingMap.getName(), hazelcast);
		result.autoSyncStartTime();
		return result;
	}

	@Override
	public DistributedScheduledFuture schedule(String taskName, Runnable runner, long delay, TimeUnit timeUnit)
			throws DistributedScheduleException {
		if (!this.hzTrackingMap.containsKey(taskName)) {
			try {
				if (this.hzTrackingMap.tryLock(taskName, 3, TimeUnit.SECONDS)) {
					try {
						IScheduledFuture<?> future = this.hzScheduler
								.schedule(new HzDistributedRunnableWrapper(this.hzScheduler.getName(), true, taskName,
										this.hzTrackingMap.getName(), runner), delay, timeUnit);
						return prepareFuture(taskName, future);
					} finally {
						this.hzTrackingMap.unlock(taskName);
					}
				}
			} catch (InterruptedException e) {
				throw new DistributedScheduleException(e);
			}
		}
		throw new DistributedScheduleException();
	}

	@Override
	public DistributedScheduledFuture scheduleAtFixedRate(String taskName, Runnable runner, long delay, long period,
			TimeUnit timeUnit) throws DistributedScheduleException {
		if (!this.hzTrackingMap.containsKey(taskName)) {
			if (this.hzTrackingMap.tryLock(taskName)) {
				try {
					IScheduledFuture<?> future = this.hzScheduler
							.scheduleAtFixedRate(new HzDistributedRunnableWrapper(this.hzScheduler.getName(), false,
									taskName, this.hzTrackingMap.getName(), runner), delay, period, timeUnit);
					return prepareFuture(taskName, future);
				} finally {
					this.hzTrackingMap.unlock(taskName);
				}
			}
		}
		throw new DistributedScheduleException();
	}

}
