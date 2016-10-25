package nhb.mario3.entity.message.transcoder.http;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import nhb.mario3.entity.message.MessageRW;
import nhb.mario3.entity.message.data.PuHttpRequestObject;

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
