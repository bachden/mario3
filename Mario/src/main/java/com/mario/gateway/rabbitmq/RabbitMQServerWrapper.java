package com.mario.gateway.rabbitmq;

import java.io.IOException;

import com.mario.config.serverwrapper.RabbitMQServerWrapperConfig;
import com.mario.gateway.serverwrapper.BaseServerWrapper;
import com.nhb.messaging.rabbit.connection.RabbitMQConnection;
import com.nhb.messaging.rabbit.connection.RabbitMQConnectionPool;

public class RabbitMQServerWrapper extends BaseServerWrapper {

	private RabbitMQConnectionPool connectionPool;

	@Override
	public void init() {
		if (this.getConfig() instanceof RabbitMQServerWrapperConfig) {
			this.connectionPool = new RabbitMQConnectionPool();
			this.connectionPool.addEndpoints(((RabbitMQServerWrapperConfig) this.getConfig()).getEndpoints());
			this.connectionPool.setCredential(((RabbitMQServerWrapperConfig) this.getConfig()).getCredential());
		}
	}

	public RabbitMQConnectionPool getConnectionPool() {
		return this.connectionPool;
	}

	public RabbitMQConnection getConnection() {
		return this.connectionPool != null ? this.connectionPool.getConnection() : null;
	}

	@Override
	public void start() {
		getLogger().info("Starting RabbitMQServerWrapper " + this.getName()
				+ ", in case the progress stop at following below line, plz check rabbitmq to make sure it's running on target server or remove the server config");
		if (this.connectionPool != null) {
			this.connectionPool.init();
		}
	}

	@Override
	public void stop() {
		try {
			this.connectionPool.close();
		} catch (IOException e) {
			System.err.println("Error while closing rabbitmq connection pool: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
