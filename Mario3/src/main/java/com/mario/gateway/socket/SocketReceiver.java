package com.mario.gateway.socket;

import com.nhb.common.Loggable;

public interface SocketReceiver extends Loggable {

	void sessionOpened(String sessionId);

	void sessionClosed(String sessionId);

	void receive(String sessionId, Object data);
}
