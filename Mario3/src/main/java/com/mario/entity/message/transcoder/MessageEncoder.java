package com.mario.entity.message.transcoder;

public interface MessageEncoder {

	Object encode(Object data) throws Exception;
}
