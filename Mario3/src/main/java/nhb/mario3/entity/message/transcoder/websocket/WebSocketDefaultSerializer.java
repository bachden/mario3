package nhb.mario3.entity.message.transcoder.websocket;

import nhb.common.data.PuObject;
import nhb.common.exception.UnsupportedTypeException;
import nhb.mario3.entity.message.transcoder.MessageEncoder;

public class WebSocketDefaultSerializer implements MessageEncoder {

	@Override
	public Object encode(Object data) throws Exception {
		if (data == null) {
			return null;
		}
		if (data instanceof PuObject) {
			return ((PuObject) data).toJSON();
		} else if (data instanceof String) {
			return (String) data;
		}
		throw new UnsupportedTypeException(
				"Object " + data + ", which type of " + data.getClass().getName() + " is not supported");
	}

}
