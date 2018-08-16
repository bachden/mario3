package com.mario.entity.message.transcoder.socket;

import java.io.InputStream;

import com.mario.entity.message.MessageRW;
import com.mario.entity.message.SocketMessage;
import com.mario.entity.message.transcoder.MessageDecodingException;
import com.mario.entity.message.transcoder.binary.BinaryMessageDeserializer;
import com.mario.gateway.socket.SocketMessageType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuElementJSONHelper;

public class SocketMessageDeserializer extends BinaryMessageDeserializer {

	@Override
	public void decode(Object data, MessageRW message) throws MessageDecodingException {
		if (data instanceof byte[]) {
			super.decode(data, message);
		} else if (data instanceof Object[]) {
			Object[] arr = (Object[]) data;
			Object body = arr[0];

			if (message instanceof SocketMessage) {
				if (arr.length > 1) {
					String sessionId = (String) arr[1];
					((SocketMessage) message).setSessionId(sessionId);
					if (arr.length > 2) {
						SocketMessageType socketMessageType = (SocketMessageType) arr[2];
						((SocketMessage) message).setSocketMessageType(socketMessageType);
					}
				}
			}

			if (body instanceof byte[] || body instanceof InputStream) {
				super.decode(body, message);
			} else if (body instanceof PuElement) {
				message.setData((PuElement) body);
			} else if (body instanceof String) {
				// websocket support
				String bodyString = (String) body;
				bodyString = bodyString.trim();
				message.setData(PuElementJSONHelper.fromJSON(bodyString));
			} else if (body instanceof Throwable) {
				throw body instanceof MessageDecodingException ? (MessageDecodingException) body
						: new MessageDecodingException(message, (Throwable) body);
			}
		}
	}
}
