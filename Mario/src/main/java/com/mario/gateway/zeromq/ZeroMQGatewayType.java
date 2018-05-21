package com.mario.gateway.zeromq;

public enum ZeroMQGatewayType {

	TASK, RPC, PUB_SUB;

	public static ZeroMQGatewayType forName(String name) {
		if (name != null) {
			name = name.trim();
			for (ZeroMQGatewayType type : values()) {
				if (type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
		}
		return null;
	}
}
