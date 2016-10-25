package nhb.mario3.entity.message.transcoder.http;

import java.util.Enumeration;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import nhb.common.data.PuObject;
import nhb.mario3.entity.message.MessageRW;

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
