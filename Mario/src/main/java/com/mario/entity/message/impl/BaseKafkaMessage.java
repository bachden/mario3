package com.mario.entity.message.impl;

import com.mario.config.gateway.GatewayType;
import com.mario.entity.message.KafkaMessage;

public class BaseKafkaMessage extends BaseMessage implements KafkaMessage {

	private byte[] key;
	private String topic;

	{
		this.setGatewayType(GatewayType.KAFKA);
	}
	
	@Override
	public void setKey(byte[] key) {
		this.key = key;
	}

	@Override
	public byte[] getKey() {
		return this.key;
	}

	@Override
	public String getTopic() {
		return this.topic;
	}

	@Override
	public void setTopic(String topic) {
		this.topic = topic;
	}

}
