package com.mario.gateway;

import com.mario.entity.message.transcoder.MessageDecoder;

public interface HasDeserializerGateway {

	void setDeserializer(MessageDecoder decoder);

	default boolean isDeserializerRequired() {
		return false;
	}
}
