package com.mario.entity.message.transcoder.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import com.mario.entity.message.MessageRW;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuElementJSONHelper;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class FullSupportHttpMessageDeserializer extends HttpMessageDeserializer {

	@Override
	protected void decodeHttpRequest(ServletRequest data, MessageRW message) {
		HttpServletRequest request = (HttpServletRequest) data;
		PuObject params = new PuObject();
		if (request != null) {

			if (request.getMethod().equalsIgnoreCase("post")) {
				String contentType = request.getContentType().toLowerCase();
				if (!contentType.contains("application/x-www-form-urlencoded")
						&& contentType.contains("multipart/form-data")) {
					try {
						Collection<Part> parts = request.getParts();
						for (Part part : parts) {
							if (part.getSize() > 0) {
								byte[] bytes = new byte[(int) part.getSize()];
								part.getInputStream().read(bytes, 0, bytes.length);
								params.setRaw(part.getName(), bytes);
							}
						}
					} catch (Exception e) {
						getLogger().error("Error while get data from request", e);
						throw new RuntimeException("Error while get data from request: " + e.getMessage(), e);
					}
				} else {
					try (InputStream is = request.getInputStream(); StringWriter sw = new StringWriter()) {
						IOUtils.copy(is, sw);
						String requestStr = sw.toString();
						PuElement pue = PuElementJSONHelper.fromJSON(requestStr);
						if (pue instanceof PuObjectRO) {
							params.addAll((PuObjectRO) pue);
						} else {
							getLogger().error("Cannot parse request as json object: '{}'", requestStr);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}

			Enumeration<String> it = request.getParameterNames();
			while (it.hasMoreElements()) {
				String key = it.nextElement();
				String value = request.getParameter(key);
				params.set(key, value);
			}
		}
		
		message.setData(params);
	}
}
