package com.mario.entity;

import com.mario.entity.message.Message;

public interface DecodeErrorHandler {

	void onDecodeError(Message message, Throwable cause);
}
