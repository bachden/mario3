package com.mario.entity;

import com.mario.entity.message.Message;
import com.mario.gateway.Gateway;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;

public interface MessageHandler extends LifeCycle, Pluggable, Loggable {

	void bind(Gateway gateway);

	/**
	 * Handle request from binded gateway(s)
	 * 
	 * @param message
	 * @return data will be sent back to client (include null), for some gateway
	 *         (rabbitmq rpc - for e.g) if result is instance of PuNull (which
	 *         has only one instance "IGNORE_ME" (include subclass) can be got
	 *         from {@link PuNull.IGNORE_ME}), it will be ignored
	 */
	PuElement handle(Message message);

	PuElement interop(PuElement requestParams);
}
