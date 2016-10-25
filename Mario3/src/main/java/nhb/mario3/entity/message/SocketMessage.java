package nhb.mario3.entity.message;

import nhb.mario3.gateway.socket.SocketMessageType;

public interface SocketMessage extends MessageForwardable {

	SocketMessageType getSocketMessageType();

	void setSocketMessageType(SocketMessageType type);

	String getSessionId();

	void setSessionId(String sessionId);
}
