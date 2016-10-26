package com.mario.exceptions;

public class MessageHandlerNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -5170355616974611356L;

	public MessageHandlerNotFoundException() {
		super("Message handler not found");
	}

	public MessageHandlerNotFoundException(String msg) {
		super(msg);
	}

	public MessageHandlerNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public MessageHandlerNotFoundException(Throwable cause) {
		super(cause);
	}
}
