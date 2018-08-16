package com.mario.entity.message.transcoder.binary;

import com.mario.entity.message.transcoder.MessageEncoder;
import com.nhb.common.data.PuElement;
import com.nhb.common.exception.UnsupportedTypeException;

public class BinaryMessageSerializer implements MessageEncoder {

	@Override
	public Object encode(Object data) throws Exception {

		if (data == null) {
			return null;
		}

		if (data instanceof String) {
			return ((String) data).getBytes();
		}

		if (data instanceof byte[] || data instanceof PuElement) {
			return data;
		}

		throw new UnsupportedTypeException("Cannot serialize data type " + data.getClass());
	}
}
