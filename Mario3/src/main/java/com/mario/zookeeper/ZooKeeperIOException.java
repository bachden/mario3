package com.mario.zookeeper;

public class ZooKeeperIOException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ZooKeeperIOException(String message) {
		super(message);
	}

	public ZooKeeperIOException(Throwable e) {
		super(e);
	}

	public ZooKeeperIOException(String message, Throwable e) {
		super(message, e);
	}
}
