package com.mario.entity.message;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class RabbitMQDeliveredMessage {

	String consumerTag;
	Envelope envelope;
	BasicProperties properties;
	byte[] body;

	public String getConsumerTag() {
		return consumerTag;
	}

	public void setConsumerTag(String consumerTag) {
		this.consumerTag = consumerTag;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}

	public BasicProperties getProperties() {
		return properties;
	}

	public void setProperties(BasicProperties properties) {
		this.properties = properties;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public RabbitMQDeliveredMessage() {
		// do nothing
	}

	public RabbitMQDeliveredMessage(String consumerTag, Envelope envelope, BasicProperties properties) {
		this();
		this.consumerTag = consumerTag;
		this.envelope = envelope;
		this.properties = properties;
	}

	public RabbitMQDeliveredMessage(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
		this(consumerTag, envelope, properties);
		this.body = body;
	}
}