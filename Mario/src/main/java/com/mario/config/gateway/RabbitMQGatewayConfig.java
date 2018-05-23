package com.mario.config.gateway;

import org.w3c.dom.Node;

import com.mario.entity.message.transcoder.binary.BinaryMessageSerializer;
import com.mario.entity.message.transcoder.rabbitmq.RabbitMQMessageDeserializer;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuElementJSONHelper;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.messaging.MessagingModel;
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

	private RabbitMQQueueConfig readQueueConfig(Node node) {
		RabbitMQQueueConfig queueConfig = null;
		if (node != null) {
			queueConfig = new RabbitMQQueueConfig();
			Node element = node.getFirstChild();
			while (element != null) {
				if (element.getNodeType() == 1) {
					String nodeName = element.getNodeName();
					String value = element.getTextContent().trim();
					if (nodeName.equalsIgnoreCase("name") || nodeName.equalsIgnoreCase("queuename")) {
						queueConfig.setQueueName(value);
					} else if (nodeName.equalsIgnoreCase("autoack")) {
						queueConfig.setAutoAck(Boolean.valueOf(element.getTextContent()));
					} else if (nodeName.equalsIgnoreCase("exchangename")) {
						queueConfig.setExchangeName(value);
					} else if (nodeName.equalsIgnoreCase("exchangetype")) {
						queueConfig.setExchangeType(value);
					} else if (nodeName.equalsIgnoreCase("routingkey")) {
						queueConfig.setRoutingKey(value);
					} else if (nodeName.equalsIgnoreCase("type") || nodeName.equalsIgnoreCase("messagingmodel")) {
						queueConfig.setType(MessagingModel.fromName(value));
					} else if (nodeName.equalsIgnoreCase("qos")) {
						queueConfig.setQos(Integer.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("durable")) {
						queueConfig.setDurable(Boolean.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("exclusive")) {
						queueConfig.setExclusive(Boolean.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("autoDelete")) {
						queueConfig.setAutoDelete(Boolean.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("variables") || nodeName.equalsIgnoreCase("arguments")) {
						queueConfig.setArguments(PuObject.fromXML(element).toMap());
					}
				}
				element = element.getNextSibling();
			}
		}
		return queueConfig;
	}

	@Override
	public void readNode(Node item) {
		Node ele = item.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == 1) {
				String value = ele.getTextContent().trim();
				if (ele.getNodeName().equalsIgnoreCase("deserializer")) {
					this.setDeserializerClassName(value);
				} else if (ele.getNodeName().equalsIgnoreCase("serializer")) {
					this.setSerializerClassName(value);
				} else if (ele.getNodeName().equalsIgnoreCase("name")) {
					this.setName(value);
				} else if (ele.getNodeName().equalsIgnoreCase("workerpool")) {
					this.setWorkerPoolConfig(readWorkerPoolConfig(ele));
				} else if (ele.getNodeName().equalsIgnoreCase("server")) {
					this.setServerWrapperName(value);
				} else if (ele.getNodeName().equalsIgnoreCase("queue")) {
					this.setQueueConfig(readQueueConfig(ele));
				} else if (ele.getNodeName().equalsIgnoreCase("ackOnError")) {
					this.setAckOnError(Boolean.valueOf(ele.getTextContent()));
				} else if (ele.getNodeName().equalsIgnoreCase("resultOnError")) {
					this.setResultOnError(PuElementJSONHelper.fromJSON(ele.getTextContent()));
				}
			}
			ele = ele.getNextSibling();
		}
	}
}
