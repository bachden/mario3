package com.mario.schedule.distributed.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.scheduledexecutor.IScheduledFuture;
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler;
import com.mario.schedule.distributed.DistributedScheduleCleaner;
import com.nhb.common.Loggable;

import lombok.AccessLevel;
import lombok.Getter;

public class HzDistributedScheduleCleaner implements DistributedScheduleCleaner, HazelcastInstanceAware, Loggable {

	@Getter(AccessLevel.PROTECTED)
	private final String schedulerName;

	@Getter
	private final String taskName;

	@Getter
	private final String trackingMapName;

	@Getter(AccessLevel.PROTECTED)
	private transient HazelcastInstance hazelcast;

	public HzDistributedScheduleCleaner(String schedulerName, String taskName, String trackingMapName) {
		if (taskName == null) {
			throw new IllegalArgumentException("Task name cannot be null");
		}
		if (trackingMapName == null) {
			throw new IllegalArgumentException("trackingMapName cannot be null");
		}
		this.schedulerName = schedulerName;
		this.taskName = taskName;
		this.trackingMapName = trackingMapName;
	}

	public HzDistributedScheduleCleaner(String schedulerName, String taskName, String trackingMapName,
			HazelcastInstance hazelcastInstance) {
		this(schedulerName, taskName, trackingMapName);
		this.setHazelcastInstance(hazelcastInstance);
	}

	protected IMap<String, String> getTrackingMap() {
		return this.getHazelcast().getMap(this.getTrackingMapName());
	}

	@Override
	public void cleanTask() {
		String urn = this.getTrackingMap().remove(this.taskName);
		if (urn != null) {
			IScheduledFuture<Object> future = null;
			try {
				future = this.getHazelcast().getScheduledExecutorService(this.getSchedulerName())
						.getScheduledFuture(ScheduledTaskHandler.of(urn));
			} catch (Exception e) {
				getLogger().error("Cannot get future for task name: " + this.getTaskName());
			}
			if (future != null) {
				try {
					future.dispose();
				} catch (Exception e) {
					getLogger().error("Cannot dispose future for task {}, scheduler {}", this.getTaskName(),
							this.getSchedulerName(), e);
				}
			}
		}
		this.getStartTimeTrackingMap().remove(this.getTaskName());
	}

	protected IMap<String, Long> getStartTimeTrackingMap() {
		return this.getHazelcast().getMap(this.getTrackingMapName() + ":startTime");
	}

	@Override
	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcast = hazelcastInstance;
	}
}
