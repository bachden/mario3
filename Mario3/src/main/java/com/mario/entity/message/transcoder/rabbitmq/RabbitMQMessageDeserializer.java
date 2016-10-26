package com.mario.entity.message.transcoder.rabbitmq;

import com.mario.entity.message.MessageRW;
import com.mario.entity.message.RabbitMQDeliveredMessage;
import com.mario.entity.message.RabbitMQMessage;
import com.mario.entity.message.transcoder.binary.BinaryMessageDeserializer;

public class RabbitMQMessageDeserializer extends BinaryMessageDeserializer {

	@Override
	public void decode(Object data, MessageRW message) {
		if (data instanceof byte[]) {
			super.decode(data, message);
		} else if (data instanceof Object[]) {
			Object[] arr = (Object[]) data;
			if (arr.length > 0) {
				if (arr[0] instanceof byte[]) {
					byte[] bytes = (byte[]) arr[0];
					super.decode(bytes, message);
				}
				if (arr[1] instanceof RabbitMQDeliveredMessage && message instanceof RabbitMQMessage) {
					((RabbitMQMessage) message).setDeliveredMessage((RabbitMQDeliveredMessage) arr[1]);
				}
			}
		}
	}
}
