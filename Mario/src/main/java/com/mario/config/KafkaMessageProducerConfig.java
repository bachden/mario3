package com.mario.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

	@Override
	public void readNode(Node node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null");
		}
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String nodeName = curr.getNodeName();
				String value = curr.getTextContent().trim();
				switch (nodeName.trim().toLowerCase()) {
				case "config":
				case "configuration":
				case "configfile":
				case "configurationfile":
					this.setConfigFile(value);
					break;
				case "topic":
					this.setTopic(value);
					break;
				case "name":
					this.setName(value);
					break;
				}
			}
			curr = curr.getNextSibling();
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
