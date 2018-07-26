package com.mario.gateway.socket;

import com.mario.config.gateway.SocketGatewayConfig;
import com.mario.entity.DecodeErrorHandler;
import com.mario.entity.message.DecodingErrorMessage;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.BaseSocketMessage;
import com.mario.entity.message.transcoder.MessageDecodingException;
import com.mario.entity.message.transcoder.socket.SocketMessageDeserializer;
import com.mario.gateway.AbstractGateway;
import com.mario.worker.MessageEventFactory;
import com.nhb.common.data.PuElement;

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
		if (this.getHandler() instanceof DecodeErrorHandler && message instanceof DecodingErrorMessage
				&& ((DecodingErrorMessage) message).getDecodingFailedCause() instanceof MessageDecodingException) {
			((DecodeErrorHandler) this.getHandler()).onDecodeError(message, exception);
		} else {
			getLogger().error("Error while handling message: ", exception);
		}
	}
}
