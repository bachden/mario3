package com.mario.gateway;

import com.mario.entity.message.transcoder.MessageEncoder;

public interface HasSerializerGateway {

	void setSerializer(MessageEncoder serializer);

	default boolean isSerializerRequired() {
		return false;
	}
}
