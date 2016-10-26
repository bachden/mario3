package com.mario.entity.message.transcoder.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.msgpack.MessagePack;

import com.mario.entity.message.MessageRW;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.msgpkg.PuElementTemplate;

public class BinaryMessageDeserializer extends BaseLoggable implements MessageDecoder {

	private static final MessagePack msgpack = new MessagePack();

	@Override
	public void decode(Object data, MessageRW message) {
		if (data != null) {
			PuElement puo;
			try {
				puo = PuElementTemplate.getInstance()
						.read(msgpack.createUnpacker(new ByteArrayInputStream((byte[]) data)), null);
				message.setData(puo);
			} catch (IOException e) {
				throw new RuntimeException("Unable to decode binary: " + new String((byte[]) data), e);
			}
		}
	}
}
