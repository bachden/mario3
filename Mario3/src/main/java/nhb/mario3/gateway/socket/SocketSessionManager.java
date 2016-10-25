package nhb.mario3.gateway.socket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.hashids.Hashids;

import nhb.eventdriven.impl.BaseEvent;
import nhb.eventdriven.impl.BaseEventDispatcher;
import nhb.mario3.statics.Fields;

public class SocketSessionManager extends BaseEventDispatcher {

	public static final String SESSION_OPENED = "sessionOpened";
	public static final String SESSION_CLOSED = "sessionClosed";

	private static AtomicLong idSeed = new AtomicLong();
	private Hashids hashids = new Hashids(UUID.randomUUID().toString(), 8,
			"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
	private Map<String, SocketSession> idToSessionMap = new ConcurrentHashMap<>();

	public String register(SocketSession session) {
		String sessionId = null;
		if (session != null) {
			if (this.idToSessionMap.containsValue(session)) {
				throw new RuntimeException("Session has been registered: " + session.getId());
			}
			sessionId = hashids.encode(idSeed.getAndIncrement());
			this.idToSessionMap.put(sessionId, session);
			this.dispatchEvent(new BaseEvent(SESSION_OPENED, Fields.SESSION_ID, sessionId));
		}
		return sessionId;
	}

	public boolean hasSession(String id) {
		return this.idToSessionMap.containsKey(id);
	}

	public SocketSession deregister(String id) {
		SocketSession session = this.idToSessionMap.remove(id);
		if (session != null) {
			this.dispatchEvent(new BaseEvent(SESSION_CLOSED, Fields.SESSION_ID, id));
		}
		return session;
	}

	public SocketSession getSessionFromId(String id) {
		if (id == null) {
			return null;
		}
		return this.idToSessionMap.get(id);
	}

	public Iterable<SocketSession> iterator() {
		return this.idToSessionMap.values();
	}
}
