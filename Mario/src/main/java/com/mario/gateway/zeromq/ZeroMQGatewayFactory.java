package com.mario.gateway.zeromq;

import com.mario.config.gateway.ZeroMQGatewayConfig;
import com.mario.gateway.Gateway;

public class ZeroMQGatewayFactory {

	public static Gateway newZeroMQGateway(ZeroMQGatewayConfig config) {
		if (config == null) {
			throw new NullPointerException("Cannot create ZeroMQGateway instance for null config");
		} else {
			switch (config.getZeroMQGatewayType()) {
			case PUB_SUB:
				return new ZeroMQPubSubGateway();
			case RPC:
				return new ZeroMQRPCGateway();
			case TASK:
				return new ZeroMQTaskGateway();
			default:
				throw new IllegalArgumentException(
						"ZeroMQGatewayType is invalid or not supported: " + config.getZeroMQGatewayType());
			}
		}
	}
}
