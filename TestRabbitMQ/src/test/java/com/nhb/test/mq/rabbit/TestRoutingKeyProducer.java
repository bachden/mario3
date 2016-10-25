package com.nhb.test.mq.rabbit;

import java.io.IOException;

import nhb.common.data.PuObject;
import nhb.common.utils.Initializer;
import nhb.common.vo.HostAndPort;
import nhb.common.vo.UserNameAndPassword;
import nhb.messaging.rabbit.RabbitMQQueueConfig;
import nhb.messaging.rabbit.connection.RabbitMQConnection;
import nhb.messaging.rabbit.connection.RabbitMQConnectionPool;
import nhb.messaging.rabbit.producer.RabbitMQRoutingProducer;

public class TestRoutingKeyProducer extends RabbitMQRoutingProducer {

	private static String EXCHANGE_NAME = "testRoutingExchange";

	public static void main(String[] args) {

		Initializer.bootstrap(TestRoutingKeyProducer.class);

		RabbitMQConnectionPool connectionPool = new RabbitMQConnectionPool();
		connectionPool.addEndpoints(new HostAndPort("localhost", 5672));
		connectionPool.setCredential(new UserNameAndPassword("root", "123456"));

		RabbitMQQueueConfig queueConfig = new RabbitMQQueueConfig();
		queueConfig.setExchangeName(EXCHANGE_NAME);
		queueConfig.setExchangeType("direct");

		final TestRoutingKeyProducer app = new TestRoutingKeyProducer(connectionPool.getConnection(), queueConfig);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				app.close();
				try {
					connectionPool.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		app.start();

		try {
			app.execute();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}

	private void execute() {
		PuObject puo = new PuObject();
		puo.set("ok", "man");
		this.publish(puo, "a");
		this.publish(puo, "b");
	}

	public TestRoutingKeyProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}

}
