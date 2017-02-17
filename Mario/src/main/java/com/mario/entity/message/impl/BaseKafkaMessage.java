package com.mario.entity.message.impl;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.mario.config.gateway.GatewayType;
import com.mario.entity.message.KafkaMessage;
import com.nhb.common.data.PuElement;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseKafkaMessage extends BaseMessage implements KafkaMessage {

	private byte[] key;
	private String topic;
	private int partition;

	private List<ConsumerRecord<byte[], PuElement>> records;

	{
		this.setGatewayType(GatewayType.KAFKA);
	}

}
