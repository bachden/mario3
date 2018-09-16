package com.mario.test.gateway.kafka;

import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.KafkaMessage;
import com.mario.entity.message.Message;
import com.nhb.common.data.PuElement;
import com.nhb.common.utils.UUIDUtils;

public class TestKafkaHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message message) {
		if (message instanceof KafkaMessage) {
			getLogger().debug("got message from topic {}, key {}, value {}", ((KafkaMessage) message).getTopic(),
					UUIDUtils.bytesToUUID(((KafkaMessage) message).getKey()), message.getData());
		}
		return null;
	}
}
