package com.mario.monitor.config;

import java.util.HashMap;
import java.util.Map;

import com.mario.monitor.MonitorableStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MonitorAlertConfig {

	@Setter
	private boolean autoSendRecovery = true;

	private final Map<MonitorableStatus, MonitorAlertStatusConfig> statusToConfigs = new HashMap<>();
}
