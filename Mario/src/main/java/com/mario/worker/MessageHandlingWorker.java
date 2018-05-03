package com.mario.worker;

import com.lmax.disruptor.WorkHandler;
import com.mario.entity.MessageHandleCallback;
import com.mario.entity.MessageHandler;
import com.mario.entity.message.DecodingErrorMessage;
import com.mario.entity.message.Message;
import com.mario.entity.message.MessageRW;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;

public class MessageHandlingWorker implements WorkHandler<Message>, Loggable {

	private MessageHandleCallback callback;
	private MessageHandler handler;

	@Override
	public void onEvent(Message message) throws Exception {
		if (this.getHandler() != null) {
			try {
				PuElement result = null;
				boolean hasError = false;
				if (message instanceof DecodingErrorMessage
						&& ((DecodingErrorMessage) message).getDecodingFailedCause() != null) {
					if (this.getCallback() != null) {
						try {
							this.getCallback().onHandleError(message,
									((DecodingErrorMessage) message).getDecodingFailedCause());
						} catch (Exception ex) {
							getLogger().error("Message decode error, but cannnot send error response to client", ex);
						}
					}
					hasError = true;
				} else {
					try {
						result = this.getHandler().handle(message);
					} catch (Exception e) {
						if (this.getCallback() != null) {
							try {
								this.getCallback().onHandleError(message, e);
							} catch (Exception ex) {
								getLogger().error("Message handling error, but cannnot send error response to client",
										ex);
							}
						}
						hasError = true;
					}
				}
				if (!hasError && this.getCallback() != null) {
					try {
						this.getCallback().onHandleComplete(message, result);
					} catch (Exception ex) {
						getLogger().error("Error while handling complete on message: {}", message.getData(), ex);
					}
				}
			} finally {
				if (message instanceof MessageRW) {
					((MessageRW) message).clear();
				}
			}
		} else {
			throw new NullPointerException("Cannot handle message without handler");
		}
	}

	public MessageHandler getHandler() {
		return handler;
	}

	public void setHandler(MessageHandler handler) {
		this.handler = handler;
	}

	public MessageHandleCallback getCallback() {
		return callback;
	}

	public void setCallback(MessageHandleCallback callback) {
		this.callback = callback;
	}

}
