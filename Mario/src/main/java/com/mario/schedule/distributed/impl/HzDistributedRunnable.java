package com.mario.schedule.distributed.impl;

import java.io.Serializable;

import com.hazelcast.core.HazelcastInstanceAware;

import lombok.Getter;

public class HzDistributedRunnable extends HzDistributedScheduleCleaner implements Runnable, Serializable {

	private static final long serialVersionUID = 7251235487019550384L;

	private final Runnable runner;

	@Getter
	private final boolean autoClean;

	public HzDistributedRunnable(String schedulerName, boolean autoClean, String taskName, String trackingMapName,
			Runnable runner) {
		super(schedulerName, taskName, trackingMapName);
		if (!(runner instanceof Serializable)) {
			throw new IllegalArgumentException("Runner must be instanceof Serializable");
		}
		this.runner = runner;
		this.autoClean = autoClean;
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
			if (this.autoClean) {
				this.cleanTask();
			}
		}
	}
}
