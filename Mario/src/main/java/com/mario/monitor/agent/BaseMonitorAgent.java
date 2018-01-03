package com.mario.monitor.agent;

import com.mario.api.MarioApi;
import com.mario.entity.Pluggable;
import com.mario.monitor.Monitorable;
import com.mario.monitor.config.MonitorAlertConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class BaseMonitorAgent implements MonitorAgent, Pluggable {

	@Setter
	@Getter
	private String name;

	@Setter
	@Getter
	private String extensionName;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private long interval = -1;

	@Setter
	@Getter
	private MarioApi api;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private MonitorAlertConfig alertConfig;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Monitorable target;
}
