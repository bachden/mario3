package com.mario.entity.message.transcoder.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.mario.entity.message.KafkaMessage;
import com.mario.entity.message.MessageRW;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.nhb.common.data.PuElement;

public class KafkaDeserializer implements MessageDecoder {

	@Override
	@SuppressWarnings("unchecked")
	public void decode(Object data, MessageRW message) {
		if (data instanceof ConsumerRecord) {
			message.setData(((ConsumerRecord<byte[], PuElement>) data).value());
			if (message instanceof KafkaMessage) {
				((KafkaMessage) message).setKey(((ConsumerRecord<byte[], PuElement>) data).key());
				((KafkaMessage) message).setTopic(((ConsumerRecord<byte[], PuElement>) data).topic());
			}
			return;
		}
		throw new IllegalArgumentException("Data must be instance of ConsumerRecord");
	}

}
