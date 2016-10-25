package nhb.mario3.config;

import nhb.mario3.config.gateway.GatewayType;

public abstract class MessageProducerConfig extends MarioBaseConfig {

	private GatewayType gatewayType;
	
	public GatewayType getGatewayType() {
		return gatewayType;
	}

	public void setGatewayType(GatewayType gatewayType) {
		this.gatewayType = gatewayType;
	}
}
