package com.mario.monitor.config;

import java.util.HashMap;
import java.util.Map;

import com.mario.monitor.MonitorableStatus;

import lombok.Getter;

@Getter
public class MonitorAlertConfig {

	private final Map<MonitorableStatus, MonitorAlertStatusConfig> statusToConfigs = new HashMap<>();
}
