package com.nhb.test.mq.rabbit;

import java.io.IOException;
import java.text.DecimalFormat;

import nhb.common.data.PuObject;
import nhb.common.utils.Initializer;
import nhb.common.vo.HostAndPort;
import nhb.common.vo.UserNameAndPassword;
import nhb.messaging.MessagingModel;
import nhb.messaging.rabbit.RabbitMQQueueConfig;
import nhb.messaging.rabbit.connection.RabbitMQConnection;
import nhb.messaging.rabbit.connection.RabbitMQConnectionPool;
import nhb.messaging.rabbit.producer.RabbitMQTaskProducer;

public class TestTaskProducer extends RabbitMQTaskProducer {

	private static String QUEUE_NAME = "tasks";

	public static void main(String[] args) {

		Initializer.bootstrap(TestTaskProducer.class);

		RabbitMQConnectionPool connectionPool = new RabbitMQConnectionPool();
		connectionPool.addEndpoints(new HostAndPort("localhost", 5672));
		connectionPool.setCredential(new UserNameAndPassword("root", "123456"));

		RabbitMQQueueConfig queueConfig = new RabbitMQQueueConfig();
		queueConfig.setQueueName(QUEUE_NAME);
		queueConfig.setType(MessagingModel.TASK_QUEUE);

		final TestTaskProducer app = new TestTaskProducer(connectionPool.getConnection(), queueConfig);

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
		DecimalFormat df = new DecimalFormat("0.##");
		long total = (long) 1e5;
		long startTime = System.nanoTime();
		for (int i = 0; i < total; i++) {
			PuObject puo = new PuObject();
			puo.set("id", i);
			this.publish(puo);
		}
		long time = System.nanoTime() - startTime;
		getLogger().debug("Total message: " + total);
		getLogger().debug("Total time: " + df.format(Double.valueOf(time) / 1e9));
		getLogger().debug("Message per second: " + df.format(Double.valueOf(total) / Double.valueOf(time / 1e9)));
	}

	public TestTaskProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}

}
