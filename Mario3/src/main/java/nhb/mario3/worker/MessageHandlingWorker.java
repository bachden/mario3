package nhb.mario3.worker;

import com.lmax.disruptor.WorkHandler;

import nhb.common.data.PuElement;
import nhb.mario3.entity.MessageHandleCallback;
import nhb.mario3.entity.MessageHandler;
import nhb.mario3.entity.message.DecodingErrorMessage;
import nhb.mario3.entity.message.Message;
import nhb.mario3.entity.message.MessageRW;

public class MessageHandlingWorker implements WorkHandler<Message> {

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
						this.getCallback().onHandleError(message,
								((DecodingErrorMessage) message).getDecodingFailedCause());
					}
					hasError = true;
				} else {
					try {
						result = this.getHandler().handle(message);
					} catch (Exception e) {
						if (this.getCallback() != null) {
							this.getCallback().onHandleError(message, e);
						}
						hasError = true;
					}
				}
				if (!hasError && this.getCallback() != null) {
					this.getCallback().onHandleComplete(message, result);
				}
			} finally {
				if (message instanceof MessageRW) {
					((MessageRW) message).clear();
				}
			}
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
