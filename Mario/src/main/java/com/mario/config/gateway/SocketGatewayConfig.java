package com.mario.config.gateway;

import org.w3c.dom.Node;

import com.mario.entity.message.transcoder.binary.BinaryMessageSerializer;
import com.mario.entity.message.transcoder.socket.SocketMessageDeserializer;
import com.mario.gateway.socket.SocketProtocol;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SocketGatewayConfig extends GatewayConfig {

	private String host = null;
	private int port = -1;
	private boolean useLengthPrepender = true;
	private SocketProtocol protocol = SocketProtocol.TCP;

	private int bootEventLoopGroupThreads = 2;
	private int workerEventLoopGroupThreads = 4;

	private String path = "/websocket";
	private String proxy = null;
	private boolean autoActiveChannel = true;

	{
		this.setType(GatewayType.SOCKET);
		this.setDeserializerClassName(SocketMessageDeserializer.class.getName());
		this.setSerializerClassName(BinaryMessageSerializer.class.getName());
	}

	@Override
	protected void _readPuObject(PuObjectRO data) {
		super._readPuObject(data);
		if (data.variableExists("host")) {
			this.setHost(data.getString("host"));
		}
		if (data.variableExists("port")) {
			this.setPort(data.getInteger("port"));
		}
		if (data.variableExists("useLengthPrepender")) {
			this.setUseLengthPrepender(data.getBoolean("useLengthPrepender"));
		}
		if (data.variableExists("protocol")) {
			this.setProtocol(SocketProtocol.fromName(data.getString("protocol")));
		}
		if (data.variableExists("path")) {
			this.setPath(data.getString("path"));
		}
		if (data.variableExists("proxy")) {
			this.setProxy(data.getString("proxy"));
		}
		if (data.variableExists("autoActiveChannel")) {
			this.setAutoActiveChannel(data.getBoolean("autoActiveChannel"));
		}
	}

	@Override
	public void readNode(Node item) {
		Node ele = item.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == 1) {
				String nodeName = ele.getNodeName();
				String value = ele.getTextContent().trim();
				if (nodeName.equalsIgnoreCase("protocol")) {
					this.setProtocol(SocketProtocol.fromName(value));
				} else if (nodeName.equalsIgnoreCase("host")) {
					this.setHost(value);
				} else if (nodeName.equalsIgnoreCase("port")) {
					this.setPort(Integer.valueOf(value));
				} else if (nodeName.equalsIgnoreCase("path")) {
					this.setPath(value);
				} else if (nodeName.equalsIgnoreCase("proxy")) {
					this.setProxy(value);
				} else if (nodeName.equalsIgnoreCase("autoActiveChannel")) {
					this.setAutoActiveChannel(Boolean.valueOf(value));
				} else if (nodeName.equalsIgnoreCase("deserializer")) {
					this.setDeserializerClassName(value);
				} else if (nodeName.equalsIgnoreCase("serializer")) {
					this.setSerializerClassName(value);
				} else if (nodeName.equalsIgnoreCase("ssl")) {
					this.setSsl(Boolean.valueOf(value));
				} else if (nodeName.equalsIgnoreCase("sslcontextname")) {
					this.setSslContextName(value);
				} else if (nodeName.equalsIgnoreCase("name")) {
					this.setName(value);
				} else if (nodeName.equalsIgnoreCase("workerpool")) {
					this.setWorkerPoolConfig(readWorkerPoolConfig(ele));
				} else if (nodeName.equalsIgnoreCase("uselengthprepender")
						|| nodeName.equalsIgnoreCase("usinglengthprepender")
						|| nodeName.equalsIgnoreCase("prependlength")) {
					this.setUseLengthPrepender(Boolean.valueOf(value));
				} else if (nodeName.equalsIgnoreCase("bootGroupThreads")) {
					this.setBootEventLoopGroupThreads(Integer.valueOf(value));
				} else if (nodeName.equalsIgnoreCase("workerGroupThreads")) {
					this.setWorkerEventLoopGroupThreads(Integer.valueOf(value));
				}
			}
			ele = ele.getNextSibling();
		}
	}
}
