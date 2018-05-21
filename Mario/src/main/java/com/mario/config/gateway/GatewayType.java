package com.mario.config.gateway;

public enum GatewayType {
	HTTP, RABBITMQ, SOCKET, KAFKA, ZEROMQ;

	public static GatewayType fromName(String name) {
		if (name != null && name.trim().length() > 0) {
			for (GatewayType type : values()) {
				if (type.name().equalsIgnoreCase(name.trim())) {
					return type;
				}
			}
		}
		return null;
	}
}
