package com.mario.monitor;

import com.mario.entity.Pluggable;
import com.mario.monitor.config.MonitorAlertConfig;

public interface MonitorAgent extends Pluggable {

	public void start();

	public void stop();

	public void setInterval(long interval);

	public void setTarget(Monitorable monitorable);

	public void setAlertConfig(MonitorAlertConfig alertConfig);
}
