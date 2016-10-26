package com.mario.entity.message.transcoder.websocket;

import com.mario.entity.message.MessageRW;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;
import com.nhb.common.exception.UnsupportedTypeException;

public class WebSocketDefaultDeserializer implements MessageDecoder {

	@Override
	public void decode(Object data, MessageRW message) {
		if (data != null) {
			if (data instanceof String) {
				String json = ((String) data).trim();
				if (json.startsWith("[")) {
					message.setData(PuArrayList.fromJSON(json));
				} else if (json.startsWith("{")) {
					message.setData(PuObject.fromJSON(json));
				}
			}
			throw new UnsupportedTypeException("Data type of " + data.getClass() + " is not supported");
		}
	}
}
