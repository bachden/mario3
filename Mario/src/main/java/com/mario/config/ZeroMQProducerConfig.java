package com.mario.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nhb.common.data.PuObjectRO;

import lombok.Getter;

@Getter
public class ZeroMQProducerConfig extends MessageProducerConfig {

	public static enum ZeroMQProducerType {
		RPC, TASK, PUB;

		public static final ZeroMQProducerType fromName(String name) {
			if (name != null) {
				name = name.trim();
				for (ZeroMQProducerType type : values()) {
					if (type.name().equalsIgnoreCase(name)) {
						return type;
					}
				}
			}
			return null;
		}
	}

	private String registryName;
	private ZeroMQProducerType type;
	private int messageBufferSize = 1024;
	private int bufferCapacity = 1024;
	private int queueSize = 1024;
	private String threadNamePattern = "producer-#%d";

	private String endpoint;
	private int numSenders = 1;

	private long hwm = (long) 1e6;
	private String receiveEndpoint; // only use for RPC
	private int receiveWorkerSize = 1;

	@Override
	public void readNode(Node node) {
		if (node == null) {
			throw new NullPointerException("Node cannot be null");
		}
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String nodeName = curr.getNodeName().trim().toLowerCase();
				String value = curr.getTextContent().trim();
				switch (nodeName) {
				case "name":
					this.setName(value);
					break;
				case "registry":
				case "registryname":
					this.registryName = value;
					break;
				case "type":
					this.type = ZeroMQProducerType.fromName(value);
					break;
				case "endpoint":
					this.endpoint = value;
					break;
				case "receiveendpoint":
					this.receiveEndpoint = value;
					break;
				case "buffercapacity":
					this.bufferCapacity = Integer.valueOf(value);
					break;
				case "messagebuffersize":
					this.messageBufferSize = Integer.valueOf(value);
					break;
				case "threadnamepattern":
					this.threadNamePattern = value;
					break;
				case "queuesize":
					this.queueSize = Integer.valueOf(value);
					break;
				case "numsenders":
					this.numSenders = Integer.valueOf(value);
					break;
				case "hwm":
					this.hwm = Long.valueOf(hwm);
					break;
				case "receiveworkersize":
					this.receiveWorkerSize = Integer.valueOf(value);
					break;
				}
			}
			curr = curr.getNextSibling();
		}
	}

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("type")) {
			this.type = ZeroMQProducerType.fromName(data.getString("type"));
		}
		if (data.variableExists("registry")) {
			this.registryName = data.getString("registry");
		}
		if (data.variableExists("endpoint")) {
			this.endpoint = data.getString("endpoint");
		}
		if (data.variableExists("numSenders")) {
			this.numSenders = data.getInteger("numSenders");
		}
		if (data.variableExists("receiveEndpoint")) {
			this.receiveEndpoint = data.getString("receiveEndpoint");
		}
		if (data.variableExists("receiveWorkerSize")) {
			this.receiveWorkerSize = data.getInteger("receiveWorkerSize");
		}
		if (data.variableExists("bufferCapacity")) {
			this.bufferCapacity = data.getInteger("bufferCapacity");
		}
		if (data.variableExists("messageBufferSize")) {
			this.messageBufferSize = data.getInteger("messageBufferSize");
		}
		if (data.variableExists("threadNamePattern")) {
			this.threadNamePattern = data.getString("threadNamePattern");
		}
		if (data.variableExists("queueSize")) {
			this.queueSize = data.getInteger("queueSize");
		}
		if (data.variableExists("hwm")) {
			this.hwm = data.getLong("hwm");
		}
	}

}
