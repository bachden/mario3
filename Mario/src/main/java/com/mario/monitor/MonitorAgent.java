package com.mario.monitor;

import com.mario.entity.LifeCycle;
import com.mario.entity.Pluggable;

public interface MonitorAgent extends LifeCycle, Pluggable {

	public void start();

	public void stop();

	public void setInterval(int interval);

	public void monitor(Monitorable monitorable);
}
