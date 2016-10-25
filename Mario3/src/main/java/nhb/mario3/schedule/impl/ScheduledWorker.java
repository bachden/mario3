package nhb.mario3.schedule.impl;

import com.lmax.disruptor.WorkHandler;

import nhb.common.BaseLoggable;

class ScheduledWorker extends BaseLoggable implements WorkHandler<ScheduledEvent> {

	@Override
	public void onEvent(ScheduledEvent event) throws Exception {
		if (event.getCallback() != null) {
			event.getCallback().call();
		}
	}
}
