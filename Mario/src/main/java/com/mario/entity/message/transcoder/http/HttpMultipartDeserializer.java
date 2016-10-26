package com.mario.entity.message.transcoder.http;

import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.mario.entity.message.MessageRW;
import com.mario.entity.message.transcoder.http.HttpMessageDeserializer;
import com.nhb.common.data.PuObject;

public class HttpMultipartDeserializer extends HttpMessageDeserializer {
	protected void deserializeHttpRequest(HttpServletRequest request, MessageRW message) {
		if (request != null) {
			getLogger().debug("{} - from {}", request.getMethod(), request.getRemoteAddr());
			PuObject puo = new PuObject();
			if (request.getMethod().equalsIgnoreCase("POST")) {
				try {
					Collection<Part> parts = request.getParts();
					for (Part part : parts) {
						if (part.getSize() > 0) {
							byte[] data = new byte[(int) part.getSize()];
							part.getInputStream().read(data, 0, data.length);
							puo.setRaw(part.getName(), data);
						}
					}
				} catch (Exception e) {
					getLogger().debug("try to get post data error", e);
				}
			}

			getLogger().debug("Request: " + puo);

			Enumeration<String> it = request.getParameterNames();
			while (it.hasMoreElements()) {
				String key = it.nextElement();
				String value = request.getParameter(key);
				puo.setString(key, value);
			}

			message.setData(puo);
		}
	}

	@Override
	protected void decodeHttpRequest(ServletRequest data, MessageRW message) {
		this.deserializeHttpRequest((HttpServletRequest) data, message);
	}

}
