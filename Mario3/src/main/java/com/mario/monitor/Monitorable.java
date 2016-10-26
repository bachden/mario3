package com.mario.monitor;

public interface Monitorable {

	String getId();

	MonitorableStatus checkStatus();

	void resume();
}
