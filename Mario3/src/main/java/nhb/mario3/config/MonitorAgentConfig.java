package nhb.mario3.config;

import nhb.common.data.PuObjectRO;
import nhb.mario3.exceptions.OperationNotSupported;

public class MonitorAgentConfig extends MarioBaseConfig {

	private PuObjectRO initParams;
	private String handleClass;
	private int interval;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		throw new OperationNotSupported();
	}

	public PuObjectRO getInitParams() {
		return initParams;
	}

	public void setInitParams(PuObjectRO initParams) {
		this.initParams = initParams;
	}

	public String getHandleClass() {
		return handleClass;
	}

	public void setHandleClass(String handleClass) {
		this.handleClass = handleClass;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
}
