package nhb.mario3.gateway.socket;

import java.io.IOException;
import java.net.InetSocketAddress;

import nhb.eventdriven.EventDispatcher;

public interface SocketSession extends EventDispatcher {

	String getId();

	InetSocketAddress getRemoteAddress();

	void send(Object obj);

	void sendPromise(Object obj) throws InterruptedException;

	boolean isActive();

	void close() throws IOException;

	void closeSync() throws IOException, InterruptedException;
}
