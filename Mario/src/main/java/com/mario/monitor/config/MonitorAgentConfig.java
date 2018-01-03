package com.mario.monitor.config;

import com.mario.config.MarioBaseConfig;
import com.mario.exceptions.OperationNotSupported;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MonitorAgentConfig extends MarioBaseConfig {

	private long interval;
	private String target;
	private PuObjectRO monitoringParams;
	private MonitorAlertConfig alertConfig;
	private String schedulerName;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		throw new OperationNotSupported();
	}
}
