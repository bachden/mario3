package com.mario.gateway.socket;

import com.mario.config.gateway.SocketGatewayConfig;
import com.mario.gateway.socket.tcp.NettyTCPSocketGateway;
import com.mario.gateway.socket.udt.NettyUDTSocketGateway;
import com.mario.gateway.websocket.NettyWebSocketGateway;

public final class SocketGatewayFactory {

	public static SocketGateway newSocketGateway(SocketGatewayConfig config) {
		if (config != null) {
			switch (config.getProtocol()) {
			case TCP:
				return new NettyTCPSocketGateway();
			case UDT:
				return new NettyUDTSocketGateway();
			case WEBSOCKET:
				return new NettyWebSocketGateway();
			default:
				break;
			}
			throw new UnsupportedOperationException(
					"Socket gateway for protocol " + config.getProtocol() + " is not supported");
		}
		return null;
	}
}
