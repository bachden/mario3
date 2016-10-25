package com.nhb.test.mq.rabbit;

import java.io.IOException;

import nhb.common.data.PuObject;
import nhb.common.utils.Initializer;
import nhb.common.vo.HostAndPort;
import nhb.common.vo.UserNameAndPassword;
import nhb.messaging.rabbit.RabbitMQQueueConfig;
import nhb.messaging.rabbit.connection.RabbitMQConnection;
import nhb.messaging.rabbit.connection.RabbitMQConnectionPool;
import nhb.messaging.rabbit.producer.RabbitMQRPCProducer;

public class TestForwardingRPCProducer extends RabbitMQRPCProducer {

	private static String QUEUE_NAME = "forwarding_rpc";

	public static void main(String[] args) {

		Initializer.bootstrap(TestForwardingRPCProducer.class);

		RabbitMQConnectionPool connectionPool = new RabbitMQConnectionPool();
		connectionPool.addEndpoints(new HostAndPort("localhost", 5672));
		connectionPool.setCredential(new UserNameAndPassword("root", "123456"));

		RabbitMQQueueConfig queueConfig = new RabbitMQQueueConfig();
		queueConfig.setQueueName(QUEUE_NAME);

		final TestForwardingRPCProducer app = new TestForwardingRPCProducer(connectionPool.getConnection(),
				queueConfig);

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

		this.start();

		PuObject puo = new PuObject();
		puo.set("ok", "man");
		puo.set("routingKey", "b");
		try {
			getLogger().debug("result: " + this.publish(puo).get());
		} catch (Exception e) {
			getLogger().error("Error: ", e);
		}
	}

	public TestForwardingRPCProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}

}
