package com.mario.schedule.distributed;

import com.hazelcast.core.HazelcastInstance;
import com.mario.schedule.distributed.exception.DistributedScheduleException;
import com.mario.schedule.distributed.impl.HzDistributedScheduler;

import java.util.concurrent.TimeUnit;

public interface DistributedScheduler {

	String getName();

	DistributedScheduledFuture getFutureByName(String taskName);

	DistributedScheduledFuture schedule(String taskName, DistributedRunnable runner, long delay, TimeUnit timeUnit)
			throws DistributedScheduleException;

	DistributedScheduledFuture scheduleAtFixedRate(String taskName, DistributedRunnable runner, long delay, long period,
			TimeUnit timeUnit) throws DistributedScheduleException;

	static DistributedScheduler newHzDistributedScheduler(String schedulerName, HazelcastInstance hazelcastInstance) {
		return new HzDistributedScheduler(schedulerName, hazelcastInstance);
	}
}
