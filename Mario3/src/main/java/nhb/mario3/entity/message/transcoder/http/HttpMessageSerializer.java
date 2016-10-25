package nhb.mario3.entity.message.transcoder.http;

import nhb.common.BaseLoggable;
import nhb.mario3.entity.message.transcoder.MessageEncoder;

public abstract class HttpMessageSerializer extends BaseLoggable implements MessageEncoder {

	@Override
	public final Object encode(Object data) throws Exception {
		return this.encodeHttpResponse(data);
	}

	protected abstract String encodeHttpResponse(Object data) throws Exception;

}
