package com.mario.test.socket.echo.server;

import nhb.common.data.PuElement;
import nhb.mario3.entity.impl.BaseMessageHandler;
import nhb.mario3.entity.message.Message;
import nhb.mario3.entity.message.SocketMessage;
import nhb.mario3.gateway.socket.SocketSession;

public class EchoSocketServerHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message msg) {
		switch (msg.getGatewayType()) {
		case HTTP:
			return msg.getData();
		case SOCKET:
			SocketMessage message = (SocketMessage) msg;
			SocketSession session = getApi().getSocketSession(message.getSessionId());
			switch (message.getSocketMessageType()) {
			case MESSAGE:
				PuElement m = message.getData();
				session.send(m);
				break;
			case CLOSED:
				getLogger().debug("session closed: " + message.getSessionId());
				break;
			case OPENED:
				getLogger().debug("new session opened: " + message.getSessionId());
				break;
			}
			break;
		default:
			getLogger().warn("gateway type not supported...");
			break;
		}
		return null;
	}
}
