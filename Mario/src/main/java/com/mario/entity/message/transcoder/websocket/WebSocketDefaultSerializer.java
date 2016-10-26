package com.mario.entity.message.transcoder.websocket;

import com.mario.entity.message.transcoder.MessageEncoder;
import com.nhb.common.data.PuObject;
import com.nhb.common.exception.UnsupportedTypeException;

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
