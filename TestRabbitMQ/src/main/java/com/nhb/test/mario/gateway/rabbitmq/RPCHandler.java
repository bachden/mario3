package com.nhb.test.mario.gateway.rabbitmq;

import nhb.common.data.PuElement;
import nhb.common.data.PuObject;
import nhb.mario3.entity.impl.BaseMessageHandler;
import nhb.mario3.entity.message.Message;

public class RPCHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message message) {
		PuObject puo = PuObject.fromObject(message.getData());
		puo.set("ack", true);
		return puo;
	}
}
