package com.mario.test.http;

import javax.servlet.ServletRequest;

import nhb.mario3.entity.message.MessageRW;
import nhb.mario3.entity.message.transcoder.MessageDecodingException;
import nhb.mario3.entity.message.transcoder.http.HttpMessageDeserializer;

public class HttpGatewayDeserialier extends HttpMessageDeserializer {

	public HttpGatewayDeserialier() {
		super();
		getLogger().debug("init deserializer");
	}

	@Override
	protected void decodeHttpRequest(ServletRequest data, MessageRW message) throws MessageDecodingException {
		throw new MessageDecodingException(message, new NullPointerException());
	}

}
