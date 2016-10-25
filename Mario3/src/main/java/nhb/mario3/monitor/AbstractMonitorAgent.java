package nhb.mario3.monitor;

import nhb.mario3.entity.impl.BaseLifeCycle;

public abstract class AbstractMonitorAgent extends BaseLifeCycle implements MonitorAgent {

	private int interval = -1;

	@Override
	public void setInterval(int interval) {
		this.interval = interval;
	}

	protected int getInterval() {
		return this.interval;
	}

}
