package com.mario.entity.message;

import com.mario.gateway.socket.SocketMessageType;

public interface SocketMessage extends MessageForwardable {

	SocketMessageType getSocketMessageType();

	void setSocketMessageType(SocketMessageType type);

	String getSessionId();

	void setSessionId(String sessionId);
}
