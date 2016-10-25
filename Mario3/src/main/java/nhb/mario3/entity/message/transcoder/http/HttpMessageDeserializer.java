package nhb.mario3.entity.message.transcoder.http;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;

import nhb.common.BaseLoggable;
import nhb.mario3.entity.message.MessageRW;
import nhb.mario3.entity.message.impl.HttpMessage;
import nhb.mario3.entity.message.transcoder.MessageDecoder;
import nhb.mario3.entity.message.transcoder.MessageDecodingException;

public abstract class HttpMessageDeserializer extends BaseLoggable implements MessageDecoder {

	@Override
	public final void decode(Object data, MessageRW message) throws MessageDecodingException {
		if (message instanceof HttpMessage) {
			if (data instanceof AsyncContext) {
				((HttpMessage) message).setContext((AsyncContext) data);
			} else {
				((HttpMessage) message).setRequest((ServletRequest) data);
			}
		}
		this.decodeHttpRequest(
				data instanceof AsyncContext ? ((AsyncContext) data).getRequest() : (ServletRequest) data, message);
	}

	protected abstract void decodeHttpRequest(ServletRequest data, MessageRW message) throws MessageDecodingException;
}
