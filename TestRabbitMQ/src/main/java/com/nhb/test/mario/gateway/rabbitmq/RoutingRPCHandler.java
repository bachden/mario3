package com.nhb.test.mario.gateway.rabbitmq;

import nhb.common.data.PuElement;
import nhb.common.data.PuObject;
import nhb.common.data.PuObjectRO;
import nhb.mario3.entity.impl.BaseMessageHandler;
import nhb.mario3.entity.message.Message;

public class RoutingRPCHandler extends BaseMessageHandler {

	private String routingKey = null;

	@Override
	public void init(PuObjectRO initParams) {
		this.routingKey = initParams.getString("routingKey");
	}

	@Override
	public PuElement handle(Message message) {
		PuObject puo = PuObject.fromObject(message.getData());
		puo.set("ack_from", this.routingKey);
		return puo;
	}
}
