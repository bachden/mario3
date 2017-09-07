package com.nhb.test.mario.gateway.rabbitmq;

import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.nhb.common.data.PuElement;

public class TaskHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message message) {
		// getLogger().debug("got message: " + message.getData());
		return null;
	}
}
