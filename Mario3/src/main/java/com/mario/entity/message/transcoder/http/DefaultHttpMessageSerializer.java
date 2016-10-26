package com.mario.entity.message.transcoder.http;

import com.nhb.common.data.PuElement;
import com.nhb.common.exception.UnsupportedTypeException;
import com.nhb.common.utils.PrimitiveTypeUtils;

public class DefaultHttpMessageSerializer extends HttpMessageSerializer {

	@Override
	protected String encodeHttpResponse(Object data) throws Exception {
		if (data == null) {
			return null;
		}
		if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(data.getClass())) {
			return PrimitiveTypeUtils.getStringValueFrom(data);
		} else if (data instanceof PuElement) {
			return ((PuElement) data).toJSON();
		}
		throw new UnsupportedTypeException(
				"Object " + data + ", which type of " + data.getClass().getName() + " is not supported");
	}

}
