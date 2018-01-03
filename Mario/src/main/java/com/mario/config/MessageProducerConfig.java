package com.mario.config;

import com.mario.config.gateway.GatewayType;

public abstract class MessageProducerConfig extends MarioBaseConfig {

	private GatewayType gatewayType;

	public GatewayType getGatewayType() {
		return gatewayType;
	}

	public void setGatewayType(GatewayType gatewayType) {
		this.gatewayType = gatewayType;
	}
}
