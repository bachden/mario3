package com.mario.entity.message.transcoder.http;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;

import com.mario.entity.message.MessageRW;
import com.mario.entity.message.impl.HttpMessage;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.mario.entity.message.transcoder.MessageDecodingException;
import com.nhb.common.BaseLoggable;

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
