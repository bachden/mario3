package com.mario.config.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

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

	@Override
	public void readNode(Node item) {
		Node ele = item.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == 1) {
				String value = ele.getTextContent().trim();
				String nodeName = ele.getNodeName();
				if (nodeName.equalsIgnoreCase("name")) {
					this.setName(value);
				} else if (nodeName.equalsIgnoreCase("serializer")) {
					this.setSerializerClassName(value);
				} else if (nodeName.equalsIgnoreCase("deserializer")) {
					this.setDeserializerClassName(value);
				} else if (nodeName.equalsIgnoreCase("workerpool")) {
					this.readWorkerPoolConfig(ele);
				} else if (nodeName.equalsIgnoreCase("config") || nodeName.equalsIgnoreCase("configuration")
						|| nodeName.equalsIgnoreCase("configFile") || nodeName.equalsIgnoreCase("configurationFile")) {
					this.setConfigFile(value);
				} else if (nodeName.equalsIgnoreCase("topics")) {
					String[] arr = value.split(",");
					for (String str : arr) {
						str = str.trim();
						if (str.length() > 0) {
							this.getTopics().add(str);
						}
					}
				} else if (nodeName.equalsIgnoreCase("pollTimeout")) {
					this.setPollTimeout(Integer.valueOf(value));
				} else if (nodeName.equalsIgnoreCase("minBatchingSize")) {
					this.setMinBatchingSize(Integer.valueOf(value));
				} else if (nodeName.equalsIgnoreCase("maxRetentionTime")) {
					this.setMaxRetentionTime(Long.valueOf(value));
				}
			}
			ele = ele.getNextSibling();
		}
	}
}
