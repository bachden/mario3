package com.mario.monitor.config;

import com.mario.monitor.MonitorableStatus;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MonitorAlertStatusConfig {
	private MonitorableStatus status;
	private MonitorAlertRecipientsConfig recipientsConfig;
	private MonitorAlertServicesConfig servicesConfig;
}
