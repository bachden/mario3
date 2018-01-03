package com.nhb.test.mario.gateway.rabbitmq;

import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

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
