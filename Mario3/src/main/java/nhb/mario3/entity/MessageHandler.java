package nhb.mario3.entity;

import nhb.common.Loggable;
import nhb.common.data.PuElement;
import nhb.mario3.entity.message.Message;
import nhb.mario3.gateway.Gateway;

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
