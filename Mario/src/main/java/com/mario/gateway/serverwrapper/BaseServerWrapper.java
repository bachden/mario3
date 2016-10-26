package com.mario.gateway.serverwrapper;

import com.mario.config.serverwrapper.ServerWrapperConfig;
import com.nhb.common.BaseLoggable;

public abstract class BaseServerWrapper extends BaseLoggable implements ServerWrapper {

	private ServerWrapperConfig config;

	private String name;

	@Override
	public void init() {
		// do nothing
	}

	@Override
	public String getName() {
		return name;
	}

	public ServerWrapperConfig getConfig() {
		return config;
	}

	public void setConfig(ServerWrapperConfig config) {
		this.config = config;
		this.name = config.getName();
	}
}
