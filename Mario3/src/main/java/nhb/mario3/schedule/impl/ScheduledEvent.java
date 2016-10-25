package nhb.mario3.schedule.impl;

import nhb.mario3.schedule.ScheduledCallback;

class ScheduledEvent {
	private ScheduledCallback callback;

	public ScheduledCallback getCallback() {
		return callback;
	}

	public void setCallback(ScheduledCallback callback) {
		this.callback = callback;
	}
}
