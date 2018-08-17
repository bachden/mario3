package com.mario.entity.message.transcoder.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mario.entity.message.MessageRW;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.mario.entity.message.transcoder.MessageDecodingException;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.msgpkg.PuElementTemplate;

public class BinaryMessageDeserializer extends BaseLoggable implements MessageDecoder {

	@Override
	public void decode(Object data, MessageRW message) throws MessageDecodingException {
		if (data != null) {
			try {
				InputStream in = null;
				if (data instanceof byte[]) {
					in = new ByteArrayInputStream((byte[]) data);
				} else if (data instanceof InputStream) {
					in = (InputStream) data;
				}

				if (in != null) {
					PuElement parsedData = PuElementTemplate.getInstance().read(in);
					message.setData(parsedData);
				} else {
					throw new RuntimeException("Cannot deserialize data which is not byte[] either InputStream: " + data.getClass());
				}
			} catch (IOException e) {
				throw new RuntimeException("Unable to decode binary: " + new String((byte[]) data), e);
			}
		}
	}
}
