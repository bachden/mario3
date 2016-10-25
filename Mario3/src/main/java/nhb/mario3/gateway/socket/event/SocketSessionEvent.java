package nhb.mario3.gateway.socket.event;

import nhb.eventdriven.impl.AbstractEvent;

public class SocketSessionEvent extends AbstractEvent {

	public static final String SESSION_CLOSED = "sessionClosed";
	private String sessionId;

	public SocketSessionEvent(String type, String sessionId) {
		this.setType(type);
		this.setSessionId(sessionId);
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
