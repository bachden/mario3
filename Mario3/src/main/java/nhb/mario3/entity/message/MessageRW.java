package nhb.mario3.entity.message;

import nhb.common.data.PuElement;
import nhb.mario3.config.gateway.GatewayType;
import nhb.mario3.entity.MessageHandleCallback;

public interface MessageRW extends Message {

	void setData(PuElement data);

	void setGatewayName(String gatewayName);

	void setGatewayType(GatewayType gatewayType);

	void setCallback(MessageHandleCallback callback);

	void clear();
}
