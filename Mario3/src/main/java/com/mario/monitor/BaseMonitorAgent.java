package com.mario.monitor;

import com.mario.schedule.ScheduledCallback;
import com.nhb.common.data.PuObjectRO;

public class BaseMonitorAgent extends AbstractMonitorAgent {

	@Override
	public void init(PuObjectRO initParams) {
		
	}

	@Override
	public void start() {
		getApi().getScheduler().scheduleAtFixedRate(getInterval(), getInterval(), new ScheduledCallback() {

			@Override
			public void call() {
				
			}
		});
	}

	@Override
	public void stop() {

	}

	@Override
	public void monitor(Monitorable monitorable) {
		
	}

}
