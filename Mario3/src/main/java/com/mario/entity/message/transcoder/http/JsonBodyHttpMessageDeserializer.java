package com.mario.entity.message.transcoder.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Enumeration;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

import com.mario.entity.message.MessageRW;
import com.nhb.common.data.PuObject;

public class JsonBodyHttpMessageDeserializer extends HttpMessageDeserializer {

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
			if (request.getMethod().equalsIgnoreCase("post")) {
				try (InputStream is = request.getInputStream(); StringWriter sw = new StringWriter()) {
					IOUtils.copy(is, sw);
					params.addAll(PuObject.fromJSON(sw.toString()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			message.setData(params);
		}
	}
}
