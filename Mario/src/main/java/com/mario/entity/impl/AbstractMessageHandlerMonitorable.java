package com.mario.entity.impl;

import com.mario.monitor.Monitorable;
import com.nhb.common.data.PuObjectRO;

public abstract class AbstractMessageHandlerMonitorable extends BaseMessageHandler implements Monitorable {

	@Override
	public void initMonitoring(PuObjectRO params) {
		// do nothing
	}

}
