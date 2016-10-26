package com.mario.gateway.socket;

public enum SocketProtocol {
	TCP, UDP, UDT, WEBSOCKET, IPC;

	public static final SocketProtocol fromName(String name) {
		if (name != null) {
			for (SocketProtocol protocol : values()) {
				if (protocol.name().equalsIgnoreCase(name)) {
					return protocol;
				}
			}
		}
		return null;
	}
}
