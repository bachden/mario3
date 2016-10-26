package com.mario.entity.message.transcoder.binary;

import com.mario.entity.message.transcoder.MessageEncoder;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.exception.UnsupportedTypeException;

public class BinaryMessageSerializer implements MessageEncoder {

	@Override
	public Object encode(Object data) throws Exception {
		if (data == null) {
			return null;
		}
		if (data instanceof byte[]) {
			return (byte[]) data;
		} else if (data instanceof PuElement) {
			return ((PuObject) data).toBytes();
		} else if (data instanceof String) {
			return ((String) data).getBytes();
		}
		throw new UnsupportedTypeException();
	}
}
