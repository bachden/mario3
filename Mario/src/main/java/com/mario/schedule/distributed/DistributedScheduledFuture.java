package com.mario.schedule.distributed;

import java.util.concurrent.TimeUnit;

public interface DistributedScheduledFuture {

	void cancel();

	void cancelNow();

	/**
	 * how many time which the schedule elasped from start
	 * 
	 * @param timeUnit
	 *            desired unit of result
	 * @return elasped time in @{timeUnit}
	 */
	long getElaspedTime(TimeUnit timeUnit);

	/**
	 *
	 * @return how many times which the callback has been scheduled in
	 */
	long getTotalRuns();

	/**
	 * How many time from start to end for callback to be scheduled
	 * 
	 * @param timeUnit
	 *            desired unit of result
	 * @return total time in desired unit
	 */
	long getTotalRunTime(TimeUnit timeUnit);

	default long getRemainingTime(TimeUnit timeUnit) {
		return this.getElaspedTime(timeUnit);
		// return this.getTotalRunTime(timeUnit) -
		// this.getElaspedTime(timeUnit);
	}

	/**
	 * String represent for this future, it useful for case you want to
	 * re-construct the future in other node
	 * 
	 * @return
	 */
	String getTaskName();
}
