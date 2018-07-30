package com.mario.zeromq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mario.config.ZMQSocketRegistryConfig;
import com.nhb.common.Loggable;
import com.nhb.messaging.zmq.ZMQSocketRegistry;

public class ZMQSocketRegistryManager implements Loggable {

	private final Map<String, ZMQSocketRegistry> map = new ConcurrentHashMap<>();

	private final Map<String, ZMQSocketRegistryConfig> nameToConfig = new ConcurrentHashMap<>();

	public void addConfig(ZMQSocketRegistryConfig config) {
		if (this.nameToConfig.containsKey(config.getName())) {
			getLogger().warn("Duplicate zeromq config name: {}, current adding from extension: ", config.getName(),
					config.getExtensionName());
		}
		this.nameToConfig.put(config.getName(), config);
	}

	public ZMQSocketRegistry getZMQSocketRegistry(String name) {
		if (!this.map.containsKey(name) && this.nameToConfig.containsKey(name)) {
			this.map.put(name, new ZMQSocketRegistry(this.nameToConfig.get(name).getNumIOThreads()));
		}
		return this.map.get(name);
	}

	public void destroy() {
		for (ZMQSocketRegistry zmqSocketRegistry : this.map.values()) {
			zmqSocketRegistry.destroy();
		}
	}
}
