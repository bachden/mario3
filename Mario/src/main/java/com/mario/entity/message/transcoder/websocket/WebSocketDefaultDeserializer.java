package com.mario.entity.message.transcoder.websocket;

import com.mario.entity.message.MessageRW;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.nhb.common.data.PuElementJSONHelper;
import com.nhb.common.exception.UnsupportedTypeException;

public class WebSocketDefaultDeserializer implements MessageDecoder {

	@Override
	public void decode(Object data, MessageRW message) {
		if (data != null) {
			if (data instanceof String) {
				message.setData(PuElementJSONHelper.fromJSON((String) data));
			}
			throw new UnsupportedTypeException("Data type of " + data.getClass() + " is not supported");
		}
	}
}
