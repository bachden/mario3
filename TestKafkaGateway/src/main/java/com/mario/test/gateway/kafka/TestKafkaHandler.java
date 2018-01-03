package com.mario.test.gateway.kafka;

import nhb.common.data.PuElement;
import nhb.common.utils.Converter;
import nhb.mario3.entity.impl.BaseMessageHandler;
import nhb.mario3.entity.message.KafkaMessage;
import nhb.mario3.entity.message.Message;

public class TestKafkaHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message message) {
		if (message instanceof KafkaMessage) {
			getLogger().debug("got message from topic {}, key {}, value {}", ((KafkaMessage) message).getTopic(),
					Converter.bytesToUUID(((KafkaMessage) message).getKey()), message.getData());
		}
		return null;
	}
}
