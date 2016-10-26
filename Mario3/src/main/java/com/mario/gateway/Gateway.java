package com.mario.gateway;

import com.mario.config.gateway.GatewayConfig;
import com.mario.entity.MessageHandleCallback;
import com.mario.entity.MessageHandler;
import com.nhb.common.Loggable;
import com.nhb.eventdriven.EventDispatcher;

public interface Gateway extends EventDispatcher, Loggable, MessageHandleCallback {

	void init(GatewayConfig config);

	void start() throws Exception;

	void stop() throws Exception;

	MessageHandler getHandler();

	void setHandler(MessageHandler handler);

	String getName();
}
