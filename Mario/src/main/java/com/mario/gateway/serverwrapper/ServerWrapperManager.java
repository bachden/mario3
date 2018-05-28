package com.mario.gateway.serverwrapper;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mario.config.serverwrapper.ServerWrapperConfig;
import com.mario.gateway.http.JettyHttpServerWrapper;
import com.mario.gateway.rabbitmq.RabbitMQServerWrapper;

public final class ServerWrapperManager {

	private final Map<String, ServerWrapper> serverWrappers = new ConcurrentHashMap<>();

	public void init(Collection<ServerWrapperConfig> configs) {
		configs.forEach(config -> {
			if (config != null) {
				String name = config.getName();
				if (name == null || name.trim().length() == 0) {
					throw new IllegalArgumentException("Server wrapper name cannot be null or empty");
				}
				ServerWrapper serverWrapper = null;
				switch (config.getType()) {
				case HTTP:
					JettyHttpServerWrapper jettyHttpServer = new JettyHttpServerWrapper();
					jettyHttpServer.setConfig(config);
					serverWrapper = jettyHttpServer;
					break;
				case RABBITMQ:
					RabbitMQServerWrapper rabbitMQServer = new RabbitMQServerWrapper();
					rabbitMQServer.setConfig(config);
					serverWrapper = rabbitMQServer;
					break;
				default:
					break;
				}
				if (serverWrapper != null) {
					serverWrapper.init();
					this.serverWrappers.put(name, serverWrapper);
				} else {
					throw new RuntimeException("Server wrapper type is unsupported: " + config.getType());
				}
			}
		});
	}

	public void start() {
		this.serverWrappers.values().forEach(serverWrapper -> {
			serverWrapper.start();
		});
	}

	public void stop() {
		this.serverWrappers.values().forEach(serverWrapper -> {
			serverWrapper.stop();
		});
	}

	@SuppressWarnings("unchecked")
	public <T extends ServerWrapper> T getServerWrapper(String serverWrapperName) {
		return (T) this.serverWrappers.get(serverWrapperName);
	}
}
