package nhb.mario3.monitor;

import nhb.common.data.PuObjectRO;
import nhb.mario3.schedule.ScheduledCallback;

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
