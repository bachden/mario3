package com.nhb.test.mario.gateway.rabbitmq;

import nhb.common.async.RPCFuture;
import nhb.common.data.PuElement;
import nhb.common.data.PuObject;
import nhb.common.data.PuObjectRO;
import nhb.mario3.entity.impl.BaseMessageHandler;
import nhb.mario3.entity.message.Message;
import nhb.mario3.entity.message.MessageForwardable;
import nhb.messaging.rabbit.producer.RabbitMQRoutingRPCProducer;

public class ForwardHandler extends BaseMessageHandler {

	private String producerName;
	private String routingKey;

	@Override
	public void init(PuObjectRO initParams) {
		getLogger().debug("initializing " + this.getClass().getName() + " instance");
		this.producerName = initParams.getString("producerName");
		this.routingKey = initParams.getString("routingKey", "a");
	}

	@Override
	public PuElement handle(Message message) {
		String routingKey = ((PuObject) message.getData()).getString("routingKey", this.routingKey);
		getLogger().debug("got message: " + message.getData() + " from " + message.getGatewayName() + " -> forward to "
				+ this.producerName + ", routing key: " + routingKey);
		RPCFuture<PuElement> result = ((MessageForwardable) message)
				.forward((RabbitMQRoutingRPCProducer) getApi().getProducer(producerName), routingKey);
		PuElement puo = null;
		try {
			puo = result.get();
		} catch (Exception e) {
			getLogger().debug("error while getting result: ", e);
		} finally {
			getLogger().debug("forwarding result: " + puo);
		}
		return puo;
	}
}
