package com.mario.gateway.zeromq;

import com.mario.Mario;
import com.mario.config.serverwrapper.ZeroMQServerWrapperConfig;
import com.mario.gateway.serverwrapper.BaseServerWrapper;
import com.nhb.messaging.zmq.ZMQSocketRegistry;

public class ZeroMQServerWrapper extends BaseServerWrapper {

	private ZMQSocketRegistry zmqSocketRegistry;

	@Override
	public void init() {
		if (!(this.getConfig() instanceof ZeroMQServerWrapperConfig)) {
			throw new RuntimeException("Illegal config, zeroMQServerWrapper expected config of "
					+ ZeroMQServerWrapperConfig.class.getName());
		}
	}

	public ZMQSocketRegistry getZmqSocketRegistry() {
		if (this.zmqSocketRegistry == null) {
			synchronized (this) {
				if (this.zmqSocketRegistry == null) {
					ZeroMQServerWrapperConfig zeroMQServerWrapperConfig = (ZeroMQServerWrapperConfig) this.getConfig();
					this.zmqSocketRegistry = Mario.getInstance().getZmqSocketRegistryManager()
							.getZMQSocketRegistry(zeroMQServerWrapperConfig.getZeroMQRegistryName());
				}
			}
		}
		return zmqSocketRegistry;
	}

	@Override
	public void start() {
		// do nothing...
	}

	@Override
	public void stop() {
		// do nothing...
	}
}
