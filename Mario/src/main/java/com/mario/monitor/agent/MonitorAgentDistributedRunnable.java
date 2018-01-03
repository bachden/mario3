package com.mario.monitor.agent;

import com.mario.Mario;
import com.mario.schedule.distributed.DistributedRunnable;
import com.nhb.common.Loggable;

import lombok.Getter;
import lombok.Setter;

public class MonitorAgentDistributedRunnable implements DistributedRunnable, Loggable {

	private static final long serialVersionUID = 9078436056743034841L;

	@Setter
	@Getter
	private String monitorAgentName;

	@Override
	public void run() {
		MonitorAgent monitorAgent = Mario.getInstance().getMonitorAgentManager().getMonitorAgent(this.monitorAgentName);
		if (monitorAgent != null) {
			monitorAgent.executeCheck();
		} else {
			getLogger().error("Cannot find the monitor agent via name " + monitorAgentName);
		}
	}
}
