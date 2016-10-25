package nhb.mario3.entity.message.transcoder.websocket;

import nhb.common.data.PuArrayList;
import nhb.common.data.PuObject;
import nhb.common.exception.UnsupportedTypeException;
import nhb.mario3.entity.message.MessageRW;
import nhb.mario3.entity.message.transcoder.MessageDecoder;

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
