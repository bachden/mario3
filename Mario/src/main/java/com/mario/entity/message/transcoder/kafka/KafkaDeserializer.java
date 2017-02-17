package com.mario.entity.message.transcoder.kafka;

import java.util.Arrays;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.mario.entity.message.KafkaMessage;
import com.mario.entity.message.MessageRW;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.mario.entity.message.transcoder.MessageDecodingException;
import com.nhb.common.data.PuElement;

public class KafkaDeserializer implements MessageDecoder {

	@Override
	@SuppressWarnings("unchecked")
	public void decode(Object data, MessageRW message) throws MessageDecodingException {
		if (data instanceof ConsumerRecord) {
			ConsumerRecord<byte[], PuElement> record = (ConsumerRecord<byte[], PuElement>) data;
			message.setData(record.value());
			if (message instanceof KafkaMessage) {
				KafkaMessage kafkaMessage = (KafkaMessage) message;

				kafkaMessage.setKey(record.key());
				kafkaMessage.setTopic(record.topic());
				kafkaMessage.setPartition(record.partition());
				kafkaMessage.setRecords(Arrays.asList(record));
			}
		} else if (data instanceof List<?> && (message instanceof KafkaMessage)) {
			KafkaMessage kafkaMessage = (KafkaMessage) message;
			try {
				kafkaMessage.setRecords((List<ConsumerRecord<byte[], PuElement>>) data);
			} catch (Exception e) {
				throw new MessageDecodingException(kafkaMessage, e);
			}
		} else {
			throw new IllegalArgumentException(
					"Data must be instance of ConsumerRecord<byte[], PuElement> or ConsumerRecords<byte[], PuElement>");
		}
	}

}
