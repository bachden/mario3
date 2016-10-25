package nhb.mario3.gateway.socket;

import nhb.mario3.config.gateway.SocketGatewayConfig;
import nhb.mario3.gateway.socket.tcp.NettyTCPSocketGateway;
import nhb.mario3.gateway.socket.udt.NettyUDTSocketGateway;
import nhb.mario3.gateway.websocket.NettyWebSocketGateway;

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
