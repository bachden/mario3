package com.mario.entity.message.transcoder.http;

import java.util.Enumeration;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.mario.entity.message.MessageRW;
import com.nhb.common.data.PuObject;

public class DefaultHttpMessageDeserializer extends HttpMessageDeserializer {

	@Override
	protected void decodeHttpRequest(ServletRequest data, MessageRW message) {
		HttpServletRequest request = (HttpServletRequest) data;
		if (request != null) {
			PuObject params = new PuObject();
			Enumeration<String> it = request.getParameterNames();
			while (it.hasMoreElements()) {
				String key = it.nextElement();
				String value = request.getParameter(key);
				params.set(key, value);
			}
			message.setData(params);
		}
	}
}
