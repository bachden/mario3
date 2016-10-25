package nhb.mario3.gateway.socket;

import nhb.mario3.gateway.Gateway;

public interface SocketGateway extends Gateway {

	void setSessionManager(SocketSessionManager manager);
}
