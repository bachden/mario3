package nhb.mario3.schedule.impl;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nhb.mario3.schedule.ScheduledCallback;
import nhb.mario3.schedule.ScheduledFuture;
import nhb.mario3.schedule.Scheduler;

final class SchedulerImpl implements Scheduler {

	private ScheduledExecutorService scheduledExecutorService;

	SchedulerImpl(ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	@Override
	public ScheduledFuture scheduleAtFixedRate(long delay, long period, ScheduledCallback callback) {
		final ScheduledFutureImpl result = new ScheduledFutureImpl(delay);
		result.setFuture(this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (result.isCancelled()) {
					return;
				}
				callback.call();
				result.setDelay(delay);
				result.updateStartTime();
			}
		}, delay, period, TimeUnit.MILLISECONDS));
		result.updateStartTime();
		return result;
	}

	@Override
	public ScheduledFuture scheduleAtFixedRate(Date startDate, long period, ScheduledCallback callback) {
		assert startDate != null && period > 0 && callback != null;
		long delay = startDate.getTime() - System.currentTimeMillis();
		assert delay > 0;
		return this.scheduleAtFixedRate(delay, period, callback);
	}

	@Override
	public ScheduledFuture schedule(long delay, ScheduledCallback callback) {
		ScheduledFutureImpl result = new ScheduledFutureImpl(delay);
		result.setFuture(this.scheduledExecutorService.schedule(new Runnable() {
			@Override
			public void run() {
				if (result.isCancelled()) {
					return;
				}
				ScheduledEventExecutor.getInstance().publish(callback);
			}
		}, delay, TimeUnit.MILLISECONDS));
		result.updateStartTime();
		return result;
	}

	@Override
	public ScheduledFuture schedule(Date date, ScheduledCallback callback) {
		assert date != null && callback != null;
		long delay = date.getTime() - System.currentTimeMillis();
		assert delay > 0;
		return this.schedule(delay, callback);
	}
}
