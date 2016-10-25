package nhb.mario3.gateway;

import nhb.common.Loggable;
import nhb.eventdriven.EventDispatcher;
import nhb.mario3.config.gateway.GatewayConfig;
import nhb.mario3.entity.MessageHandleCallback;
import nhb.mario3.entity.MessageHandler;

public interface Gateway extends EventDispatcher, Loggable, MessageHandleCallback {

	void init(GatewayConfig config);

	void start() throws Exception;

	void stop() throws Exception;

	MessageHandler getHandler();

	void setHandler(MessageHandler handler);

	String getName();
}
