package com.mario.config.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mario.entity.message.transcoder.kafka.KafkaDeserializer;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KafkaGatewayConfig extends GatewayConfig {

	{
		this.setType(GatewayType.KAFKA);
		this.setDeserializerClassName(KafkaDeserializer.class.getName());
	}

	private String configFile;
	private int pollTimeout = 10;
	private int minBatchingSize = 0;
	private long maxRetentionTime = 100;
	private final List<String> topics = new ArrayList<>();

	@Override
	protected void _readPuObject(PuObjectRO data) {
		super._readPuObject(data);
		if (data.variableExists("configFile")) {
			this.setConfigFile(data.getString("configFile"));
		} else if (data.variableExists("config")) {
			this.setConfigFile(data.getString("config"));
		}

		if (data.variableExists("pollTimeout")) {
			this.setPollTimeout(data.getInteger("pollTimeout"));
		}

		if (data.variableExists("topics")) {
			String topicsString = data.getString("topics");
			String[] topics = topicsString.split(",");
			this.topics.addAll(Arrays.asList(topics));
		}
	}

}
