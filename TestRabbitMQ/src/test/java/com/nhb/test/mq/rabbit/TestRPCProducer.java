package com.nhb.test.mq.rabbit;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

import com.nhb.common.async.Callback;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.utils.Initializer;
import com.nhb.common.vo.HostAndPort;
import com.nhb.common.vo.UserNameAndPassword;
import com.nhb.messaging.MessagingModel;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;
import com.nhb.messaging.rabbit.connection.RabbitMQConnection;
import com.nhb.messaging.rabbit.connection.RabbitMQConnectionPool;
import com.nhb.messaging.rabbit.producer.RabbitMQRPCProducer;

public class TestRPCProducer extends RabbitMQRPCProducer {

	private static String QUEUE_NAME = "rpc";

	public static void main(String[] args) {

		Initializer.bootstrap(TestRPCProducer.class);

		final RabbitMQConnectionPool connectionPool = new RabbitMQConnectionPool();
		connectionPool.addEndpoints(new HostAndPort("localhost", 5672));
		connectionPool.setCredential(new UserNameAndPassword("root", "123456"));

		RabbitMQQueueConfig queueConfig = new RabbitMQQueueConfig();
		queueConfig.setQueueName(QUEUE_NAME);
		queueConfig.setType(MessagingModel.TASK_QUEUE);

		final TestRPCProducer app = new TestRPCProducer(connectionPool.getConnection(), queueConfig);

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

	private void execute() throws InterruptedException {

		final DecimalFormat df = new DecimalFormat("0.##");
		int numThreads = 200;
		final int total = 100000;

		final CountDownLatch startSignal = new CountDownLatch(1);
		final CountDownLatch doneSignal = new CountDownLatch(total);

		Thread monitor = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}

					if (doneSignal.getCount() == 0) {
						return;
					}
					getLogger().debug("Remaining: " + doneSignal.getCount() + " on " + total + " (complete "
							+ df.format(Double.valueOf(total - doneSignal.getCount()) * 100 / total) + "%)");
				}
			};
		};

		final int messagePerThread = new Double(Double.valueOf(total) / Double.valueOf(numThreads)).intValue();

		for (int i = 0; i < numThreads; i++) {
			new Thread() {
				@Override
				public void run() {
					byte[][] arr = new byte[messagePerThread][];
					for (int i = 0; i < messagePerThread; i++) {
						PuObject puo = new PuObject();
						puo.set("id", i);
						arr[i] = puo.toBytes();
					}
					try {
						startSignal.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					for (int i = 0; i < messagePerThread; i++) {
						publish(arr[i]).setCallback(new Callback<PuElement>() {

							@Override
							public void apply(PuElement result) {
								doneSignal.countDown();
							}
						});
					}
				}
			}.start();
		}

		long startTime = System.nanoTime();
		startSignal.countDown();
		monitor.start();
		doneSignal.await();
		long time = System.nanoTime() - startTime;

		getLogger("pureLogger").info("\n*********** REPORT ***********");
		getLogger("pureLogger").info("Total time: " + df.format(Double.valueOf(time / 1e9)) + "s");
		getLogger("pureLogger").info("Total message: " + total);
		getLogger("pureLogger").info("Num threads: " + numThreads);
		getLogger("pureLogger").info("Num message per thread: " + messagePerThread);
		getLogger("pureLogger")
				.info("Message per second: " + df.format(new Double(total) / Double.valueOf(time / 1e9)));
	}

	public TestRPCProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
		super(connection, queueConfig);
	}

}
