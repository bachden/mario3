package nhb.mario3.gateway;

import nhb.eventdriven.impl.AbstractEvent;

public class GatewayEvent extends AbstractEvent {

	public static final String BEFORE_START = "beforeStart";
	public static final String STARTED = "started";
	public static final String ALL_GATEWAY_READY = "allGatewayReady";

	public static GatewayEvent createBeforeStartEvent() {
		return new GatewayEvent(BEFORE_START);
	}

	public static GatewayEvent createAllGatewayReadyEvent() {
		return new GatewayEvent(ALL_GATEWAY_READY);
	}

	public static GatewayEvent createStartedEvent() {
		return new GatewayEvent(STARTED);
	}

	public GatewayEvent(String type) {
		this.setType(type);
	}
}
