package nhb.mario3.gateway.serverwrapper;

import nhb.common.BaseLoggable;
import nhb.mario3.config.serverwrapper.ServerWrapperConfig;

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
