package nhb.mario3.gateway.socket;

import nhb.common.data.PuElement;
import nhb.mario3.config.gateway.SocketGatewayConfig;
import nhb.mario3.entity.message.Message;
import nhb.mario3.entity.message.impl.BaseSocketMessage;
import nhb.mario3.entity.message.transcoder.socket.SocketMessageDeserializer;
import nhb.mario3.gateway.AbstractGateway;
import nhb.mario3.worker.MessageEventFactory;

public abstract class BaseSocketGateway extends AbstractGateway<SocketGatewayConfig> implements SocketGateway {

	private Thread startThread = null;

	{
		this.setDeserializer(new SocketMessageDeserializer());
	}

	private SocketSessionManager sessionManager;

	protected MessageEventFactory createEventFactory() {
		return new MessageEventFactory() {

			@Override
			public Message newInstance() {
				return new BaseSocketMessage();
			}
		};
	}

	@Override
	protected final void _start() throws Exception {
		if (startThread == null) {
			startThread = new Thread() {
				@Override
				public void run() {
					__start();
				}
			};
			startThread.start();
		} else {
			throw new IllegalAccessException("Gateway has been started");
		}
	}

	@Override
	protected final void _stop() {
		this.__stop();
		if (this.startThread != null) {
			if (this.startThread.isAlive()) {
				this.startThread.interrupt();
			}
			this.startThread = null;
		}
	}

	protected abstract void __stop();

	protected abstract void __start();

	public SocketSessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SocketSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public void onHandleComplete(Message message, PuElement result) {
		// do nothing...
	}

	@Override
	public void onHandleError(Message message, Throwable exception) {
		getLogger().error("Error while handling message: ", exception);
	}
}
