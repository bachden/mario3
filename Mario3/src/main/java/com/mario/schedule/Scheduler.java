package com.mario.schedule;

import java.util.Date;

public interface Scheduler {

	public ScheduledFuture schedule(long delay, ScheduledCallback callback);

	public ScheduledFuture schedule(Date date, ScheduledCallback callback);

	/**
	 * 
	 * @param delay
	 *            (ms) delay for the first times execute
	 * @param period
	 *            (ms) interval time
	 * @param callback
	 *            callback on timer event
	 * @return
	 */
	public ScheduledFuture scheduleAtFixedRate(long delay, long period, ScheduledCallback callback);

	/**
	 * 
	 * @param startDate
	 *            (ms) start time for the first execution
	 * @param period
	 *            (ms) interval time
	 * @param callback
	 *            callback on timer event
	 * @return
	 */
	public ScheduledFuture scheduleAtFixedRate(Date startDate, long period, ScheduledCallback callback);
}
