package com.nhb.test.mario.gateway.rabbitmq;

import nhb.common.data.PuElement;
import nhb.mario3.entity.impl.BaseMessageHandler;
import nhb.mario3.entity.message.Message;

public class TaskHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message message) {
		// getLogger().debug("got message: " + message.getData());
		return null;
	}
}
