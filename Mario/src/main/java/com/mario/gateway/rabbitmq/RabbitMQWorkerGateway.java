package com.mario.gateway.rabbitmq;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.mario.entity.message.Message;
import com.mario.entity.message.MessageForwardable;
import com.mario.entity.message.RabbitMQDeliveredMessage;
import com.mario.entity.message.RabbitMQMessage;
import com.mario.entity.message.impl.BaseRabbitMQMessage;
import com.mario.worker.MessageEventFactory;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;
import com.nhb.messaging.MessageForwarder;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.rabbit.producer.RabbitMQProducer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

public class RabbitMQWorkerGateway extends RabbitMQGateway {

	@Override
	protected MessageEventFactory createEventFactory() {
		return new MessageEventFactory() {
			@Override
			public Message newInstance() {
				BaseRabbitMQMessage result = new BaseRabbitMQMessage();
				result.setGatewayName(getName());
				result.setGatewayType(getConfig().getType());
				result.setCallback(RabbitMQWorkerGateway.this);
				prepareForwarder(result);
				return result;
			}
		};
	}

	private Set<Long> unackedDeliveryTags = new CopyOnWriteArraySet<>();

	protected final void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
			byte[] body) {

		RabbitMQDeliveredMessage deliveredMessage = new RabbitMQDeliveredMessage(consumerTag, envelope, properties,
				body);

		try {
			this.publishToWorkers(new Object[] { body, deliveredMessage });
		} catch (Exception e) {
			tryHandleError(deliveredMessage, e);
		}
	}

	protected final void ack(long deliveryTag) {
		if (!this.getConfig().getQueueConfig().isAutoAck()) {
			try {
				getChannel().basicAck(deliveryTag, false);
				if (getUnackedDeliveryTags().contains(deliveryTag)) {
					getUnackedDeliveryTags().remove(deliveryTag);
				}
			} catch (IOException e) {
				getLogger().error("Cannot send ack for deliveryTag: " + deliveryTag, e);
				getUnackedDeliveryTags().add(deliveryTag);
			}
		}
	}

	private void tryHandleError(RabbitMQDeliveredMessage mesProps, Throwable ex) {
		try {
			if (this.getConfig().getResultOnError() != null) {
				getLogger().debug("Error while handle message, default resultOnError will be handled as result", ex);
				this.handleResult(mesProps.getConsumerTag(), mesProps.getEnvelope(), mesProps.getProperties(),
						this.getConfig().getResultOnError());
			} else {
				getLogger().error(
						"An error occur while handling message, you may want to config resultOnError in config of gateway name {}, extension {}",
						this.getConfig().getName(), this.getConfig().getExtensionName(), ex);
			}
		} finally {
			if (this.getConfig().isAckOnError()) {
				getLogger().info("Sending ack on error, gateway name {}, extension {}", this.getConfig().getName(),
						this.getConfig().getExtensionName());
				ack(mesProps.getEnvelope().getDeliveryTag());
			}
		}
	}

	@Override
	public final void onHandleError(Message message, Throwable exception) {
		if (message instanceof RabbitMQMessage) {
			RabbitMQMessage rabbitMQMessage = (RabbitMQMessage) message;
			RabbitMQDeliveredMessage mesProps = rabbitMQMessage.getDeliveredMessage();
			if (mesProps != null) {
				tryHandleError(mesProps, exception);
			} else {
				getLogger().error("Error while handling message without message properties, gateway {}, extension {}",
						this.getConfig().getName(), this.getConfig().getExtensionName(), exception);
			}
		}
	}

	@Override
	public final void onHandleComplete(Message message, PuElement result) {
		if (result instanceof PuNull) {
			// ignore by handler, will be manually completed
			return;
		}
		if (message instanceof RabbitMQMessage) {
			RabbitMQMessage rabbitMQMessage = (RabbitMQMessage) message;
			RabbitMQDeliveredMessage mesProps = rabbitMQMessage.getDeliveredMessage();
			if (mesProps != null) {
				ack(mesProps.getEnvelope().getDeliveryTag());
				this.handleResult(mesProps.getConsumerTag(), mesProps.getEnvelope(), mesProps.getProperties(), result);
			}
		}
	}

	protected void handleResult(String consumerTag, Envelope envelope, BasicProperties properties, PuElement result) {
		// do nothing
	}

	public Collection<Long> getUnackedDeliveryTags() {
		return unackedDeliveryTags;
	}

	protected void prepareForwarder(MessageForwardable message) {
		if (!(message instanceof RabbitMQMessage)) {
			return;
		}

		final RabbitMQMessage rabbitMQMessage = (RabbitMQMessage) message;
		rabbitMQMessage.setForwarder(new MessageForwarder() {

			@Override
			public <T extends MessageProducer<?>> void forwardAndForget(T producer, String routingKey) {
				if (producer != null) {
					RabbitMQDeliveredMessage dm = rabbitMQMessage.getDeliveredMessage();
					if (dm != null && producer instanceof RabbitMQProducer<?>) {
						RabbitMQProducer<?> rabbitMQProducer = (RabbitMQProducer<?>) producer;
						rabbitMQProducer.forward(dm.getBody(), dm.getProperties(), routingKey);
					} else {
						producer.publish(message.getData(), routingKey);
					}
				}
			}

			@Override
			public <T extends MessageProducer<?>> void forwardAndForget(T producer) {
				if (producer != null) {
					RabbitMQDeliveredMessage dm = rabbitMQMessage.getDeliveredMessage();
					if (dm != null && producer instanceof RabbitMQProducer<?>) {
						RabbitMQProducer<?> rabbitMQProducer = (RabbitMQProducer<?>) producer;
						rabbitMQProducer.forward(dm.getBody(), dm.getProperties(), null);
					} else {
						producer.publish(message.getData());
					}
				}
			}

			@SuppressWarnings("unchecked")
            @Override
			public <E, T extends MessageProducer<E>> E forward(T producer, String routingKey) {
				if (producer != null) {
					RabbitMQDeliveredMessage dm = rabbitMQMessage.getDeliveredMessage();
					if (dm != null && producer instanceof RabbitMQProducer<?>) {
						RabbitMQProducer<E> rabbitMQProducer = (RabbitMQProducer<E>) producer;
						return rabbitMQProducer.forward(dm.getBody(), dm.getProperties(), routingKey);
					} else {
						return producer.publish(message.getData(), routingKey);
					}
				}
				return null;
			}

			@Override
			public <E, T extends MessageProducer<E>> E forward(T producer) {
				if (producer != null) {
					RabbitMQDeliveredMessage dm = rabbitMQMessage.getDeliveredMessage();
					if (dm != null && producer instanceof RabbitMQProducer<?>) {
						@SuppressWarnings("unchecked")
                        RabbitMQProducer<E> rabbitMQProducer = (RabbitMQProducer<E>) producer;
						return rabbitMQProducer.forward(dm.getBody(), dm.getProperties(), null);
					} else {
						return producer.publish(message.getData());
					}
				}
				return null;
			}
		});
	}
}
