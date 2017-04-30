package com.mario.monitor.config;

import java.util.Collection;
import java.util.HashSet;

import lombok.Getter;

@Getter
public class MonitorAlertRecipientsConfig {

	private final Collection<String> contacts = new HashSet<>();
	private final Collection<String> groups = new HashSet<>();
}
