package com.mario.monitor;

public enum MonitorableStatus {

	OK, WARNING, CRITICAL, UNKNOWN;

	public static final MonitorableStatus fromName(String name) {
		if (name != null) {
			for (MonitorableStatus value : values()) {
				if (value.name().equalsIgnoreCase(name)) {
					return value;
				}
			}
		}
		return null;
	}
}
