package com.mario.config.serverwrapper;

import com.mario.config.MarioBaseConfig;

public abstract class ServerWrapperConfig extends MarioBaseConfig {

	private ServerWrapperType type;

	public static enum ServerWrapperType {
		RABBITMQ, HTTP, ZEROMQ;

		public static final ServerWrapperType fromName(String name) {
			if (name != null) {
				for (ServerWrapperType type : values()) {
					if (type.name().equalsIgnoreCase(name.trim())) {
						return type;
					}
				}
			}
			return null;
		}
	}

	public ServerWrapperType getType() {
		return type;
	}

	protected void setType(ServerWrapperType type) {
		this.type = type;
	}
}
