package com.mario.gateway.socket;

import com.mario.gateway.Gateway;

public interface SocketGateway extends Gateway {

	void setSessionManager(SocketSessionManager manager);
}
