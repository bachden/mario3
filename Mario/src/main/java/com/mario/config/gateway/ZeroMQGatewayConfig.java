package com.mario.config.gateway;

import java.util.ArrayList;
import java.util.List;

import com.mario.gateway.zeromq.ZeroMQGatewayType;

import lombok.Getter;
import lombok.Setter;

public class ZeroMQGatewayConfig extends GatewayConfig {

	@Setter
	@Getter
	private ZeroMQGatewayType zeroMQGatewayType;

	@Getter
	@Setter
	private String endpoint = null;

	@Getter
	private final List<String> subKeys = new ArrayList<>();
}
