package com.mario.config;

import com.mario.config.gateway.GatewayType;
import com.nhb.common.data.PuObjectRO;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;

public class RabbitMQProducerConfig extends MessageProducerConfig {

	{
		this.setGatewayType(GatewayType.RABBITMQ);
	}

	private int timeout;
	private RabbitMQQueueConfig queueConfig;
	private String connectionName;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("timeout")) {
			this.setTimeout(data.getInteger("timeout"));
		}

		if (data.variableExists("connectionName")) {
			this.setConnectionName(data.getString("connectionName"));
		} else if (data.variableExists("server")) {
			this.setConnectionName(data.getString("server"));
		}

		if (data.variableExists("queueConfig")) {
			if (this.queueConfig == null) {
				this.queueConfig = new RabbitMQQueueConfig();
			}
			this.queueConfig.readPuObject(data.getPuObject("queueConfig"));
		}
	}

	public RabbitMQQueueConfig getQueueConfig() {
		return queueConfig;
	}

	public void setQueueConfig(RabbitMQQueueConfig queueConfig) {
		this.queueConfig = queueConfig;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}
}
