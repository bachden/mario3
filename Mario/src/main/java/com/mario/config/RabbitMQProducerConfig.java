package com.mario.config;

import org.w3c.dom.Node;

import com.mario.config.gateway.GatewayType;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.messaging.MessagingModel;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
public class RabbitMQProducerConfig extends MessageProducerConfig {

	{
		this.setGatewayType(GatewayType.RABBITMQ);
	}

	private int timeout;
	private RabbitMQQueueConfig queueConfig;
	private String connectionName;

	private void readQueueConfig(Node node) {
		if (node != null) {
			this.queueConfig = new RabbitMQQueueConfig();
			Node element = node.getFirstChild();
			while (element != null) {
				if (element.getNodeType() == 1) {
					String nodeName = element.getNodeName();
					String value = element.getTextContent().trim();
					switch (nodeName.toLowerCase()) {
					case "name":
					case "queuename":
						queueConfig.setQueueName(value);
						break;
					case "autoack":
						queueConfig.setAutoAck(Boolean.valueOf(element.getTextContent()));
						break;
					case "exchangename":
						queueConfig.setExchangeName(value);
						break;
					case "exchangetype":
						queueConfig.setExchangeType(value);
						break;
					case "routingkey":
						queueConfig.setRoutingKey(value);
						break;
					case "type":
					case "messagingmodel":
						queueConfig.setType(MessagingModel.fromName(value));
						break;
					case "qos":
						queueConfig.setQos(Integer.valueOf(value));
						break;
					case "durable":
						queueConfig.setDurable(Boolean.valueOf(value));
						break;
					case "exclusive":
						queueConfig.setExclusive(Boolean.valueOf(value));
						break;
					case "autodelete":
						queueConfig.setAutoDelete(Boolean.valueOf(value));
						break;
					case "variables":
					case "arguments":
						queueConfig.setArguments(PuObject.fromXML(element).toMap());
						break;
					}
				}
				element = element.getNextSibling();
			}
		}

	}

	@Override
	public void readNode(Node node) {
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == 1) {
				String nodeName = curr.getNodeName();
				String value = curr.getTextContent().trim();
				switch (nodeName.toLowerCase()) {
				case "name":
					this.setName(value);
					break;
				case "server":
					this.setConnectionName(value);
					break;
				case "timeout":
					this.setTimeout(Integer.valueOf(value));
					break;
				case "queue":
					this.readQueueConfig(curr);
					break;
				}
			}
			curr = curr.getNextSibling();
		}
	}

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
}
