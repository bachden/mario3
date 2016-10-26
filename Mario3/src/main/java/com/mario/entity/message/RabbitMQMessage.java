package com.mario.entity.message;

public interface RabbitMQMessage extends MessageForwardable {

	void setDeliveredMessage(RabbitMQDeliveredMessage deliveredMessage);

	RabbitMQDeliveredMessage getDeliveredMessage();
}
