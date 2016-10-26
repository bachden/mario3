package com.mario.config.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mario.entity.message.transcoder.kafka.KafkaDeserializer;
import com.nhb.common.data.PuObjectRO;

public class KafkaGatewayConfig extends GatewayConfig {

	{
		this.setType(GatewayType.KAFKA);
		this.setDeserializerClassName(KafkaDeserializer.class.getName());
	}

	private String configFile;
	private int pollTimeout = 100;
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

	public String getConfigFile() {
		return this.configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public List<String> getTopics() {
		return topics;
	}

	public int getPollTimeout() {
		return pollTimeout;
	}

	public void setPollTimeout(int pollTimeout) {
		this.pollTimeout = pollTimeout;
	}

}
