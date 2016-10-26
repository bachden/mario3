package com.mario.entity.message.transcoder.http;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.mario.entity.message.MessageRW;
import com.mario.entity.message.data.PuHttpRequestObject;

public class LightweightHttpMessageDeserializer extends HttpMessageDeserializer {

	@Override
	protected void decodeHttpRequest(ServletRequest data, MessageRW message) {
		HttpServletRequest request = (HttpServletRequest) data;
		if (request != null) {
			if (message.getData() != null && message.getData() instanceof PuHttpRequestObject) {
				((PuHttpRequestObject) message.getData()).setRequest(request);
			} else {
				message.setData(new PuHttpRequestObject((HttpServletRequest) data));
			}
		}
	}
}
