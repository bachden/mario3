package com.mario.gateway.kafka;

import com.mario.config.gateway.KafkaGatewayConfig;

public class KafkaGatewayFactory {

	public static KafkaGateway newKafkaGateway(KafkaGatewayConfig config) {
		KafkaGateway gateway = new KafkaGateway();
		return gateway;
	}
}
