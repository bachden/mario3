package com.mario.gateway.rabbitmq;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.mario.config.gateway.RabbitMQGatewayConfig;
import com.mario.gateway.AbstractGateway;
import com.mario.gateway.serverwrapper.HasServerWrapper;
import com.nhb.messaging.rabbit.RabbitMQChannelHandleDelegate;
import com.nhb.messaging.rabbit.RabbitMQChannelHandler;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public abstract class RabbitMQGateway extends AbstractGateway<RabbitMQGatewayConfig>
		implements RabbitMQChannelHandler, RabbitMQBindableGateway, HasServerWrapper<RabbitMQServerWrapper> {

	private String queueName;

	private RabbitMQChannelHandleDelegate channelHandlerDelegate;
	private Set<String> waitingForBindRoutingKeys = new HashSet<>();

	private RabbitMQServerWrapper serverWrapper;

	@Override
	protected final void _init() {
		channelHandlerDelegate = new RabbitMQChannelHandleDelegate(this.serverWrapper.getConnection(), this);
	}

	@Override
	protected final void _start() throws Exception {
		this.channelHandlerDelegate.start();
	}

	@Override
	protected final void _stop() throws Exception {
		System.out.println("Stopping channel handler delegate...");
		this.channelHandlerDelegate.close();
	}

	protected Channel getChannel() {
		return this.channelHandlerDelegate.getChannel();
	}

	@Override
	public void setServer(RabbitMQServerWrapper server) {
		this.serverWrapper = server;
	}

	protected RabbitMQServerWrapper getServerWrapper() {
		return this.serverWrapper;
	}

	protected void initQueue() throws IOException {

		Channel channel = this.getChannel();
		RabbitMQQueueConfig queueConfig = this.getConfig().getQueueConfig();

		// declare queue
		this.queueName = queueConfig.getQueueName();
		if (queueName != null && queueName.trim().length() > 0) {
			channel.queueDeclare(queueName, queueConfig.isDurable(), queueConfig.isExclusive(),
					queueConfig.isAutoDelete(), queueConfig.getArguments());
		} else {
			queueName = channel.queueDeclare().getQueue();
		}

		// setting Qos (perfect count) value
		if (queueConfig.getQos() >= 0) {
			channel.basicQos(queueConfig.getQos());
		}

		// bind to an exchange
		if (!queueConfig.getExchangeName().isEmpty()) {
			channel.exchangeDeclare(queueConfig.getExchangeName(), queueConfig.getExchangeType());
			if (queueConfig.getRoutingKey() != null) {
				// only execute by when routing key is specific
				this.queueBind(queueConfig.getRoutingKey());
			}
			if (waitingForBindRoutingKeys.size() > 0) {
				for (String routingKey : waitingForBindRoutingKeys) {
					if (!routingKey.equals(queueConfig.getRoutingKey())) {
						this.queueBind(routingKey);
					}
				}
			}
		}
	}

	public boolean isReady() {
		return this.getChannel() != null;
	}

	@Override
	public final void onChannelReady(Channel channel) throws IOException {
		this.initQueue();
		channel.basicConsume(this.getQueueName(), getConfig().getQueueConfig().isAutoAck(),
				new DefaultConsumer(channel) {

					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
							byte[] body) throws IOException {
						RabbitMQGateway.this.handleDelivery(consumerTag, envelope, properties, body);
					}
				});
	}

	protected abstract void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
			byte[] body);

	public String getQueueName() {
		return this.queueName;
	}

	@Override
	public void queueBind(String rountingKey) throws IOException {
		if (this.isReady()) {
			if (this.getConfig().getQueueConfig().getExchangeName().isEmpty()) {
				getLogger().warn("Gateway " + this.getName()
						+ " has an empty exchange name, binding a new rountingKey may cause the unexpected errors");
			}
			getChannel().queueBind(this.getQueueName(), this.getConfig().getQueueConfig().getExchangeName(),
					rountingKey);
		} else {
			this.waitingForBindRoutingKeys.add(rountingKey);
		}
	}

	@Override
	public void queueUnbind(String rountingKey) throws IOException {
		if (!this.isReady()) {
			this.waitingForBindRoutingKeys.remove(rountingKey);
			return;
		}
		if (this.getConfig().getQueueConfig().getExchangeName().isEmpty()) {
			getLogger().warn("Gateway " + this.getName()
					+ " has an empty exchange name, unbinding a rountingKey may cause the unexpected errors");
		}
		getChannel().queueUnbind(this.getQueueName(), this.getConfig().getQueueConfig().getExchangeName(), rountingKey);
	}

}
