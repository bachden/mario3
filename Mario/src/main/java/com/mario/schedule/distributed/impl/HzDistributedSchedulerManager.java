package com.mario.schedule.distributed.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.hazelcast.core.HazelcastInstance;
import com.mario.api.MarioApi;
import com.mario.schedule.distributed.DistributedScheduler;
import com.mario.schedule.distributed.impl.config.HzDistributedSchedulerConfig;
import com.nhb.common.Loggable;

import lombok.Getter;
import lombok.Setter;

public class HzDistributedSchedulerManager implements Loggable {

	private Map<String, HzDistributedScheduler> hzSchedulers = new ConcurrentHashMap<>();

	@Setter
	@Getter
	private MarioApi api;

	private Map<String, Object> synchronizedMonitorObjects = new ConcurrentHashMap<>();

	private Map<String, HzDistributedSchedulerConfig> configs = new ConcurrentHashMap<>();

	private AtomicBoolean initialized = new AtomicBoolean(false);

	public void init(List<HzDistributedSchedulerConfig> configs) {
		getLogger().debug("Initializing " + this.getClass().getSimpleName() + "...");
		if (this.initialized.compareAndSet(false, true)) {
			if (configs != null) {
				for (HzDistributedSchedulerConfig config : configs) {
					this.configs.put(config.getName(), config);
				}
			}
		} else {
			throw new RuntimeException("Cannot re-init " + this.getClass().getSimpleName() + " instance");
		}
	}

	private Object getMonitorObject(String name) {
		Object obj = new Object();
		Object old = this.synchronizedMonitorObjects.putIfAbsent(name, obj);
		return old == null ? obj : old;
	}

	public DistributedScheduler getDistributedScheduler(String name) {
		if (!this.initialized.get()) {
			throw new IllegalStateException(this.getClass().getSimpleName()
					+ " hasn't initialized, cannot arcquire DistributedScheduler instance");
		}

		if (!this.hzSchedulers.containsKey(name)) {
			synchronized (this.getMonitorObject(name)) {
				if (!this.hzSchedulers.containsKey(name)) {
					HzDistributedSchedulerConfig config = this.configs.get(name);
					if (config == null) {
						throw new NullPointerException("Distributed scheduler cannot be acquired via name " + name);
					}
					HazelcastInstance hazelcastInstance = this.getApi().getHazelcastInstance(config.getHazelcastName());
					if (hazelcastInstance == null) {
						throw new NullPointerException(
								"Hazelcast instance cannot be acquired via name " + config.getHazelcastName());
					}
					HzDistributedScheduler scheduler = new HzDistributedScheduler(config.getName(), hazelcastInstance);
					this.hzSchedulers.put(name, scheduler);
				}
			}
		}
		return this.hzSchedulers.get(name);
	}
}
