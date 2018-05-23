package com.mario.config.gateway;

import org.w3c.dom.Node;

import com.mario.config.MarioBaseConfig;
import com.mario.config.WorkerPoolConfig;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class GatewayConfig extends MarioBaseConfig {

	private String serverWrapperName;
	private String deserializerClassName;
	private String serializerClassName;
	private GatewayType type;

	private boolean ssl = false;
	private String sslContextName;

	private WorkerPoolConfig workerPoolConfig;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("serverWrapperName")) {
			this.setServerWrapperName(data.getString("serverWrapperName"));
		} else if (data.variableExists("server")) {
			this.setServerWrapperName(data.getString("server"));
		}

		if (data.variableExists("deserializerClassName")) {
			this.setDeserializerClassName(data.getString("deserializerClassName"));
		} else if (data.variableExists("deserializer")) {
			this.setDeserializerClassName(data.getString("deserializer"));
		}

		if (data.variableExists("serializerClassName")) {
			this.setSerializerClassName(data.getString("serializerClassName"));
		} else if (data.variableExists("serializer")) {
			this.setSerializerClassName(data.getString("serializer"));
		}

		if (data.variableExists("ssl")) {
			this.setSsl(data.getBoolean("ssl"));
		}

		if (data.variableExists("workerPool")) {
			if (this.workerPoolConfig == null) {
				this.workerPoolConfig = new WorkerPoolConfig();
			}
			this.workerPoolConfig.readPuObject(data.getPuObject("workerPool"));
		}
	}

	protected WorkerPoolConfig readWorkerPoolConfig(Node node) {
		WorkerPoolConfig workerPoolConfig = null;
		if (node != null) {
			workerPoolConfig = new WorkerPoolConfig();
			Node element = node.getFirstChild();
			while (element != null) {
				if (element.getNodeType() == 1) {
					String value = element.getTextContent().trim();
					String nodeName = element.getNodeName();
					if (nodeName.equalsIgnoreCase("poolsize")) {
						workerPoolConfig.setPoolSize(Integer.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("ringbuffersize")) {
						workerPoolConfig.setRingBufferSize(Integer.valueOf(value));
					} else if (nodeName.equalsIgnoreCase("threadnamepattern")) {
						workerPoolConfig.setThreadNamePattern(value);
					}
				}
				element = element.getNextSibling();
			}
		}
		return workerPoolConfig;
	}
}
