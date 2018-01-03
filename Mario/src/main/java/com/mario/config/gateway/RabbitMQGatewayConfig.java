package com.mario.config.gateway;

import com.mario.entity.message.transcoder.binary.BinaryMessageSerializer;
import com.mario.entity.message.transcoder.rabbitmq.RabbitMQMessageDeserializer;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RabbitMQGatewayConfig extends GatewayConfig {

	{
		this.setType(GatewayType.RABBITMQ);
		this.setDeserializerClassName(RabbitMQMessageDeserializer.class.getName());
		this.setSerializerClassName(BinaryMessageSerializer.class.getName());
	}

	private RabbitMQQueueConfig queueConfig;
	private String serverWrapperName;
	private boolean ackOnError = true;
	private PuElement resultOnError = null;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data != null) {
			super._readPuObject(data);

			String serverName = data.getString("server", data.getString("serverWrapperName", null));
			this.setServerWrapperName(serverName);

			PuObject queueConfigPuo = data.getPuObject("queueConfig", null);
			if (queueConfigPuo != null) {
				if (this.queueConfig == null) {
					this.queueConfig = new RabbitMQQueueConfig();
				}
				this.queueConfig.readPuObject(queueConfigPuo);
			}

			this.setAckOnError(data.getBoolean("ackOnError", true));

			if (data.variableExists("resultOnError")) {
				this.setResultOnError(data.get("resultOnError"));
			}
		}
	}

}
