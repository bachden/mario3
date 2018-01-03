package com.nhb.test.mario.gateway.rabbitmq;

import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;

public class RPCHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message message) {
		PuObject puo = PuObject.fromObject(message.getData());
		puo.set("ack", true);
		return puo;
	}
}
