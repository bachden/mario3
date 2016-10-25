package nhb.mario3.gateway.kafka;

import nhb.mario3.config.gateway.KafkaGatewayConfig;

public class KafkaGatewayFactory {

	public static KafkaGateway newKafkaGateway(KafkaGatewayConfig config) {
		KafkaGateway gateway = new KafkaGateway();
		return gateway;
	}
}
