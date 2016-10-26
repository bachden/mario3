package com.mario.config.gateway;

import com.mario.entity.message.transcoder.binary.BinaryMessageSerializer;
import com.mario.entity.message.transcoder.socket.SocketMessageDeserializer;
import com.mario.gateway.socket.SocketProtocol;
import com.nhb.common.data.PuObjectRO;

public class SocketGatewayConfig extends GatewayConfig {

	private String host = null;
	private int port = -1;
	private boolean useLengthPrepender = true;
	private SocketProtocol protocol = SocketProtocol.TCP;

	private int bootEventLoopGroupThreads = 2;
	private int workerEventLoopGroupThreads = 4;

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
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public SocketProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(SocketProtocol protocol) {
		this.protocol = protocol;
	}

	public boolean isUseLengthPrepender() {
		return useLengthPrepender;
	}

	public void setUseLengthPrepender(boolean useLengthPrepender) {
		this.useLengthPrepender = useLengthPrepender;
	}

	public int getBootEventLoopGroupThreads() {
		return bootEventLoopGroupThreads;
	}

	public void setBootEventLoopGroupThreads(int bootEventLoopGroupThreads) {
		this.bootEventLoopGroupThreads = bootEventLoopGroupThreads;
	}

	public int getWorkerEventLoopGroupThreads() {
		return workerEventLoopGroupThreads;
	}

	public void setWorkerEventLoopGroupThreads(int workerEventLoopGroupThreads) {
		this.workerEventLoopGroupThreads = workerEventLoopGroupThreads;
	}

}
