package com.mario.schedule.distributed.impl.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lombok.Getter;

public class HzDistributedSchedulerConfigManager {

	@Getter
	private List<HzDistributedSchedulerConfig> configs = new CopyOnWriteArrayList<>();

	public void read(Node node) {
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				if (curr.getNodeName().equalsIgnoreCase("hazelcast")) {
					readHzDistributedSchedulerConfig(curr);
				}
			}
			curr = curr.getNextSibling();
		}
	}

	private void readHzDistributedSchedulerConfig(Node node) {
		Node curr = node.getFirstChild();
		HzDistributedSchedulerConfig config = new HzDistributedSchedulerConfig();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String nodeName = curr.getNodeName().toLowerCase();
				String nodeValue = curr.getTextContent().trim();
				switch (nodeName) {
				case "name":
					config.setName(nodeValue);
					break;
				case "hazelcastname":
					config.setHazelcastName(nodeValue);
					break;
				}
			}
			curr = curr.getNextSibling();
		}
		if (config.getName() == null || config.getHazelcastName() == null) {
			throw new RuntimeException("Hazelcast Distributed Scheduler Config must have both name and hazelcast name");
		}
		this.configs.add(config);
	}

}
