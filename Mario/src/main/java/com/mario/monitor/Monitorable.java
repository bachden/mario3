package com.mario.monitor;

import com.nhb.common.data.PuObjectRO;

public interface Monitorable {

	void initMonitoring(PuObjectRO params);

	MonitorableResponse checkStatus();
}
