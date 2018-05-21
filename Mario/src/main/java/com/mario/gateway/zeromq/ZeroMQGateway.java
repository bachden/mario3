package com.mario.gateway.zeromq;

import com.mario.config.gateway.GatewayConfig;
import com.mario.config.gateway.ZeroMQGatewayConfig;
import com.mario.entity.MessageHandler;
import com.mario.gateway.Gateway;
import com.mario.gateway.serverwrapper.HasServerWrapper;
import com.mario.gateway.zeromq.metadata.ZeroMQInputMetadataProcessor;
import com.nhb.eventdriven.impl.BaseEventDispatcher;
import com.nhb.messaging.zmq.ZMQSocketRegistry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class ZeroMQGateway extends BaseEventDispatcher
		implements HasServerWrapper<ZeroMQServerWrapper>, Gateway, ZeroMQInputMetadataProcessor {

	@Getter
	private String name;

	@Setter
	@Getter
	private MessageHandler handler;

	private ZeroMQServerWrapper server;

	@Getter(AccessLevel.PROTECTED)
	private ZeroMQGatewayConfig config;

	@Override
	public final void init(GatewayConfig config) {
		if (!(config instanceof ZeroMQGatewayConfig)) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " init method expected config of type "
					+ ZeroMQGatewayConfig.class);
		}
		this.config = (ZeroMQGatewayConfig) config;
		this.name = config.getName();
		this._init();
	}

	protected abstract void _init();

	protected final ZMQSocketRegistry getZMQSocketRegistry() {
		if (this.server == null) {
			getLogger().warn("",
					new NullPointerException("ZeroMQServer require for ZeroMQServerWrapper, but it's null"));
			return null;
		}
		return this.server.getZmqSocketRegistry();
	}

	@Override
	public final void setServer(ZeroMQServerWrapper server) {
		if (this.server != null) {
			throw new IllegalStateException("Cannot re-set server instance of ZeroMQGateway");
		} else if (server == null) {
			throw new NullPointerException("Server instance cannot be null");
		}
		this.server = server;
	}
}
