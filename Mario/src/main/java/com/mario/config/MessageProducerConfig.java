package com.mario.config;

import org.w3c.dom.Node;

import com.mario.config.gateway.GatewayType;

import lombok.Getter;
import lombok.Setter;

public abstract class MessageProducerConfig extends MarioBaseConfig {

	@Setter
	@Getter
	private GatewayType gatewayType;

	public void readNode(Node node) {
		// do nothing
	}
}
