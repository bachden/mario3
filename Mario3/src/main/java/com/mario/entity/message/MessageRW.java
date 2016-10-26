package com.mario.entity.message;

import com.mario.config.gateway.GatewayType;
import com.mario.entity.MessageHandleCallback;
import com.nhb.common.data.PuElement;

public interface MessageRW extends Message {

	void setData(PuElement data);

	void setGatewayName(String gatewayName);

	void setGatewayType(GatewayType gatewayType);

	void setCallback(MessageHandleCallback callback);

	void clear();
}
