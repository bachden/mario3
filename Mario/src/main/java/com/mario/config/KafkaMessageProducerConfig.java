package com.mario.config;

import com.mario.config.gateway.GatewayType;
import com.nhb.common.data.PuObjectRO;

public class KafkaMessageProducerConfig extends MessageProducerConfig {

	{
		this.setGatewayType(GatewayType.KAFKA);
	}

	private String configFile;
	private String topic;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("configFile")) {
			this.setConfigFile(data.getString("configFile"));
		}
		if (data.variableExists("topic")) {
			this.setTopic(data.getString("topic"));
		}
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
}
