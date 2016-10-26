package com.mario.entity.message.transcoder;

import com.mario.entity.message.MessageRW;

public interface MessageDecoder {

	void decode(Object data, MessageRW message) throws MessageDecodingException;
}
