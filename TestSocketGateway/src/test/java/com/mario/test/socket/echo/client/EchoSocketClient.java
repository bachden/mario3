package com.mario.test.socket.echo.client;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

import nhb.common.data.MapTuple;
import nhb.common.data.PuElement;
import nhb.common.data.PuObject;
import nhb.common.utils.Initializer;
import nhb.eventdriven.Event;
import nhb.eventdriven.EventHandler;
import nhb.eventdriven.impl.BaseEventHandler;
import nhb.messaging.TransportProtocol;
import nhb.messaging.socket.SocketEvent;
import nhb.messaging.socket.netty.NettySocketClient;

public class EchoSocketClient extends NettySocketClient {

	public static void main(String[] args) {

		Initializer.bootstrap(EchoSocketClient.class);

		final EchoSocketClient client = new EchoSocketClient();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					client.close();
				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		});

		try {
			client.setUseLengthPrepender(false);
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private EventHandler onConnectedHandler = new BaseEventHandler(this, "onConnectedHandler");
	private EventHandler onDisconnectedHandler = new BaseEventHandler(this, "onDisconnectedHandler");

	private EchoSocketClient() {
		this.addEventListener(SocketEvent.CONNECTED, this.onConnectedHandler);
		this.addEventListener(SocketEvent.DISCONNECTED, this.onDisconnectedHandler);
	}

	private void start() throws IOException {
		String host = System.getProperty("server.host", "localhost");
		int port = Integer.valueOf(System.getProperty("server.port", "9999"));
		this.setProtocol(TransportProtocol.TCP);
		this.connect(host, port);
	}

	private void sendPing() throws Exception {
		DecimalFormat df = new DecimalFormat("0.##");
		int numMessages = (int) 1024 * 1024;
		int numThreads = 1;
		int messagePerThread = numMessages / numThreads;

		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch doneSignal = new CountDownLatch(messagePerThread * numThreads);

		new Thread() {
			{
				this.setName("Monitor");
			}

			@Override
			public void run() {
				try {
					startSignal.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				while (true) {
					getLogger().debug("Remaining: {} on total {}, complete {}%", doneSignal.getCount(), numMessages,
							df.format(Double.valueOf(numMessages - doneSignal.getCount()) * 100 / numMessages));
					if (doneSignal.getCount() == 0) {
						return;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();

		for (int i = 0; i < numThreads; i++) {
			new Thread() {
				@Override
				public void run() {

					PuElement[] arr = new PuElement[messagePerThread];
					for (int i = 0; i < arr.length; i++) {
						arr[i] = PuObject.fromObject(new MapTuple<>("id", 0, "name", "Nguyen Hoang Bach", "data",
								"2015-11-19 19:32:47.905 [nioEventLoopGroup-2-1] DEBUG "
										+ "com.mario.test.socket.echo.client.EchoSocketClient (EchoSocketClient.java:143"
										+ ") - Connected to server at localhost:9999 --> start ping2015-11-19 "
										+ "19:32:47.905 [nioEventLoopGroup-2-1] DEBUG com.mario.test.socket.echo"
										+ ".client.EchoSocketClient (EchoSocketClient.java:143) - Connected to serv"
										+ "er at localhost:9999 --> start ping2015-11-19 19:32:47.905 [nioEventLoopGr"
										+ "oup-2-1] DEBUG com.mario.test.socket.echo.client.EchoSocketClient (EchoSoc"
										+ "ketClient.java:143) - Connected to server at localhost:9999 --> start ping"
										+ "2015-11-19 19:32:47.905 [nioEventLoopGroup-2-1] DEBUG com.mario.test.socket."
										+ "echo.client.EchoSocketClient (EchoSocketClient.java:143) - Connected to ser"
										+ "ver at localhost:9999 --> start ping2015-11-19 19:32:47.905 [nioEventLoopGro"
										+ "up-2-1] DEBUG com.mario.test.socket.echo.client.EchoSocketClient (EchoSocket"
										+ "Client.java:143) - Connected to server at localhost:9999 --> start ping2015-1"
										+ "1-19 19:32:47.905 [nioEventLoopGroup-2-1] DEBUG com.mario.test.socket.echo.cl"
										+ "ient.EchoSocketClient (EchoSocketClient.java:143) - Connected to server at lo"
										+ "calhost:9999 --> start ping"));
					}
					try {
						startSignal.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}

					for (int i = 0; i < arr.length; i++) {
						send(arr[i]);
					}
				}
			}.start();
		}

		this.addEventListener(SocketEvent.MESSAGE, new EventHandler() {

			@Override
			public void onEvent(Event event) throws Exception {
				// SocketEvent socketEvent = (SocketEvent) event;
				// getLogger().debug("Client got message: " + socketEvent.getData());
				doneSignal.countDown();
			}
		});

		startSignal.countDown();
		long startTime = System.nanoTime();
		doneSignal.await();

		long time = System.nanoTime() - startTime;

		int messageSize = PuObject.fromObject(new MapTuple<>("id", 0, "name", "Nguyen Hoang Bach", "data",
				"2015-11-19 19:32:47.905 [nioEventLoopGroup-2-1] DEBUG "
						+ "com.mario.test.socket.echo.client.EchoSocketClient (EchoSocketClient.java:143"
						+ ") - Connected to server at localhost:9999 --> start ping2015-11-19 "
						+ "19:32:47.905 [nioEventLoopGroup-2-1] DEBUG com.mario.test.socket.echo"
						+ ".client.EchoSocketClient (EchoSocketClient.java:143) - Connected to serv"
						+ "er at localhost:9999 --> start ping2015-11-19 19:32:47.905 [nioEventLoopGr"
						+ "oup-2-1] DEBUG com.mario.test.socket.echo.client.EchoSocketClient (EchoSoc"
						+ "ketClient.java:143) - Connected to server at localhost:9999 --> start ping"
						+ "2015-11-19 19:32:47.905 [nioEventLoopGroup-2-1] DEBUG com.mario.test.socket."
						+ "echo.client.EchoSocketClient (EchoSocketClient.java:143) - Connected to ser"
						+ "ver at localhost:9999 --> start ping2015-11-19 19:32:47.905 [nioEventLoopGro"
						+ "up-2-1] DEBUG com.mario.test.socket.echo.client.EchoSocketClient (EchoSocket"
						+ "Client.java:143) - Connected to server at localhost:9999 --> start ping2015-1"
						+ "1-19 19:32:47.905 [nioEventLoopGroup-2-1] DEBUG com.mario.test.socket.echo.cl"
						+ "ient.EchoSocketClient (EchoSocketClient.java:143) - Connected to server at lo"
						+ "calhost:9999 --> start ping"))
				.toBytes().length;

		getLogger("pureLogger").info("************ REPORT ************");
		getLogger("pureLogger").info("Num message: {}", numMessages);
		getLogger("pureLogger").info("Executing time: {}s", df.format(Double.valueOf(time / 1e9)));
		getLogger("pureLogger").info("Number of threads: {}", numThreads);
		getLogger("pureLogger").info("Message size: {}byte = {}kb", messageSize,
				df.format(Double.valueOf(messageSize) / 1024));
		getLogger("pureLogger").info("Throughput: {}Mb/s",
				df.format(Double.valueOf(messageSize) / Double.valueOf(time / 1e9)));
		getLogger("pureLogger").info("Message per second: {}",
				df.format(Double.valueOf(numMessages) / Double.valueOf(time / 1e9)));

		System.exit(0);
	}

	public void onConnectedHandler(Event event) {
		getLogger().debug("Connected to server at " + this.getServerAddress() + " --> start ping");
		new Thread() {
			public void run() {
				try {
					sendPing();
				} catch (Exception e) {
					getLogger().error("Send ping error", e);
					System.exit(1);
				}
			}
		}.start();
	}

	public void onDisconnectedHandler(Event event) {
		getLogger().debug("Disconnected to server at " + this.getServerAddress());
		System.exit(0);
	}
}
