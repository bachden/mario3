package com.nhb.test.mario.gateway.rabbitmq;

import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObjectRO;

public class RoutingHandler extends BaseMessageHandler {

	private String routingKey = null;

	@Override
	public void init(PuObjectRO initParams) {
		this.routingKey = initParams.getString("routingKey");
	}

	@Override
	public PuElement handle(Message message) {
		getLogger().debug("handled on routing key: " + this.routingKey + ", message: " + message.getData());
		return null;
	}
}
