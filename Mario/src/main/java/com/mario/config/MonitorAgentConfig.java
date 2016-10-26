package com.mario.config;

import com.mario.exceptions.OperationNotSupported;
import com.nhb.common.data.PuObjectRO;

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
