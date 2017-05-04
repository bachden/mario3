package com.mario.schedule.distributed.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.mario.schedule.distributed.DistributedScheduleCleaner;

import lombok.AccessLevel;
import lombok.Getter;

public class HzDistributedScheduleCleaner implements DistributedScheduleCleaner, HazelcastInstanceAware {

	@Getter
	private final String taskName;

	@Getter
	private final String trackingMapName;

	@Getter(AccessLevel.PROTECTED)
	private transient HazelcastInstance hazelcast;

	public HzDistributedScheduleCleaner(String taskName, String trackingMapName) {
		if (taskName == null) {
			throw new IllegalArgumentException("Task name cannot be null");
		}
		if (trackingMapName == null) {
			throw new IllegalArgumentException("trackingMapName cannot be null");
		}
		this.taskName = taskName;
		this.trackingMapName = trackingMapName;
	}

	public HzDistributedScheduleCleaner(String taskName, String trackingMapName, HazelcastInstance hazelcastInstance) {
		this(taskName, trackingMapName);
		this.setHazelcastInstance(hazelcastInstance);
	}

	@Override
	public void cleanTask() {
		this.getHazelcast().getMap(this.trackingMapName).remove(this.taskName);
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
