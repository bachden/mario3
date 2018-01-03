package com.mario.exceptions;

public class InvalidConfigException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidConfigException() {
		super();
	}

	public InvalidConfigException(String message) {
		super(message);
	}

	public InvalidConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidConfigException(Throwable cause) {
		super(cause);
	}
}
