package com.mario.entity.message;

import com.mario.config.gateway.GatewayType;
import com.mario.entity.MessageHandleCallback;
import com.nhb.common.data.PuElement;
import com.nhb.strategy.CommandRequestParameters;

public interface Message extends CommandRequestParameters {

	PuElement getData();

	String getGatewayName();

	GatewayType getGatewayType();

	MessageHandleCallback getCallback();
}
