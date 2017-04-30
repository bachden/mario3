package com.mario.monitor.config;

import java.util.Collection;
import java.util.HashSet;

import lombok.Getter;

@Getter
public class MonitorAlertServicesConfig {

	private final Collection<String> smsServices = new HashSet<>();
	private final Collection<String> emailServices = new HashSet<>();
}
