package com.nhb.test.mario.gateway.rabbitmq;

import nhb.common.data.PuElement;
import nhb.common.data.PuObjectRO;
import nhb.mario3.entity.impl.BaseMessageHandler;
import nhb.mario3.entity.message.Message;

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
