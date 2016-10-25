package nhb.mario3.entity.message;

import nhb.common.data.PuElement;
import nhb.mario3.config.gateway.GatewayType;
import nhb.mario3.entity.MessageHandleCallback;
import nhb.strategy.CommandRequestParameters;

public interface Message extends CommandRequestParameters {

	PuElement getData();

	String getGatewayName();

	GatewayType getGatewayType();

	MessageHandleCallback getCallback();
}
