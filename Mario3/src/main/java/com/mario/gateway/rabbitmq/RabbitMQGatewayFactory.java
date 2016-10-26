package com.mario.gateway.rabbitmq;

import com.mario.config.gateway.RabbitMQGatewayConfig;

public class RabbitMQGatewayFactory {

	public static RabbitMQGateway newRabbitGateway(RabbitMQGatewayConfig config) {
		if (config != null) {
			switch (config.getQueueConfig().getType()) {
			case RPC:
			case TOPIC_RPC:
			case ROUTING_RPC:
				return new RabbitMQRPCGateway();
			case TOPIC:
			case ROUTING:
			case PUB_SUB:
			case TASK_QUEUE:
				return new RabbitMQWorkerGateway();
			default:
				break;
			}
		}
		throw new UnsupportedOperationException(
				config.getQueueConfig().getType() + " queue type for RabbitMQ gateway is not supported rightnow");
	}
}
