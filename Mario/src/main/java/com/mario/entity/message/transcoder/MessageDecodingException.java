package com.mario.entity.message.transcoder;

import com.mario.entity.message.Message;

public class MessageDecodingException extends Exception {

	private static final long serialVersionUID = 1L;

	private final Message target;

	public MessageDecodingException(String message) {
		super(message);
		this.target = null;
	}

	public MessageDecodingException(Message target) {
		this.target = target;
	}

	public MessageDecodingException(Message target, Throwable cause) {
		super(cause);
		this.target = target;
	}

	public Message getTarget() {
		return target;
	}
}
