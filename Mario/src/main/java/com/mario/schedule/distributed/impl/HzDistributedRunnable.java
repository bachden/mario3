package com.mario.schedule.distributed.impl;

import java.io.Serializable;

import com.hazelcast.core.HazelcastInstanceAware;

public class HzDistributedRunnable extends HzDistributedScheduleCleaner implements Runnable, Serializable {

	private static final long serialVersionUID = 7251235487019550384L;

	private final Runnable runner;

	public HzDistributedRunnable(String schedulerName, String taskName, String trackingMapName, Runnable runner) {
		super(schedulerName, taskName, trackingMapName);
		if (!(runner instanceof Serializable)) {
			throw new IllegalArgumentException("Runner must be instanceof Serializable");
		}
		this.runner = runner;
	}

	@Override
	public void run() {
		if (this.runner instanceof HazelcastInstanceAware) {
			((HazelcastInstanceAware) this.runner).setHazelcastInstance(this.getHazelcast());
		}

		try {
			this.runner.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			this.cleanTask();
		}
	}
}
