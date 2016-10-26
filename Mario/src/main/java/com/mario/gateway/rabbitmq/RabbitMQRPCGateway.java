package com.mario.gateway.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;
import com.rabbitmq.client.Envelope;

public class RabbitMQRPCGateway extends RabbitMQWorkerGateway {

	protected BasicProperties getReplyProperties(BasicProperties properties) {
		Builder builder = new BasicProperties.Builder();
		builder.correlationId(properties.getCorrelationId());
		return builder.build();
	}

	@Override
	protected void handleResult(String consumerTag, Envelope envelope, BasicProperties properties, PuElement result) {
		if (result instanceof PuNull) {
			// ignore
			return;
		}
		String replyQueue = properties.getReplyTo();
		if (replyQueue != null && replyQueue.trim().length() > 0) {
			byte[] response = result == null ? null : result.toBytes();
			BasicProperties replyProperties = this.getReplyProperties(properties);

			try {
				getChannel().basicPublish("", replyQueue, replyProperties, response);
			} catch (IOException e) {
				getLogger().error("Cannot send response to producer, queue name: " + replyQueue, e);
			}
		}
	}
}
