package com.mario.test.socket.echo.client;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuObject;
import com.nhb.common.utils.Initializer;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventHandler;
import com.nhb.eventdriven.impl.BaseEventHandler;
import com.nhb.messaging.TransportProtocol;
import com.nhb.messaging.socket.SocketEvent;
import com.nhb.messaging.socket.netty.NettySocketClient;

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

	private void send() throws Exception {

		DecimalFormat df = new DecimalFormat("0.##");

		int numMessages = 100;// (int) 1024 * 1024;
		int numThreads = 1;
		int messagePerThread = numMessages / numThreads;

		if (messagePerThread * numThreads < numMessages) {
			numThreads += 1;
		}

		String str = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
				+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

		PuObject puo = PuObject.fromObject(new MapTuple<>("id", 0, "name", "Nguyen Hoang Bach", "data", str));
		int messageSize = puo.toBytes().length;
		System.out.println("Preparing " + numMessages + " message with size " + messageSize + " bytes");

		PuObject[] messages = new PuObject[numMessages];
		for (int i = 0; i < messages.length; i++) {
			messages[i] = puo.deepClone();
		}

		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch doneSignal = new CountDownLatch(messagePerThread * numThreads);

		this.addEventListener(SocketEvent.MESSAGE, new EventHandler() {

			@Override
			public void onEvent(Event event) throws Exception {
				SocketEvent socketEvent = (SocketEvent) event;
				System.out.println("got data: " + socketEvent.getData());
				doneSignal.countDown();
			}
		});

		for (int i = 0; i < numThreads; i++) {
			final int startIndex = i * messagePerThread;
			new Thread(new Runnable() {

				@Override
				public void run() {
					int endIndex = startIndex + messagePerThread;
					if (endIndex > numMessages) {
						endIndex = numMessages;
					}

					try {
						startSignal.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}

					for (int i = startIndex; i < endIndex; i++) {
						send(messages[i]);
					}
				}
			}, "Sending thread " + (i + 1)).start();
		}

		Thread monitorThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					long count = doneSignal.getCount();
					if (count == 0) {
						System.out.println("*** DONE ***");
						return;
					}
					String percentage = df.format(Double.valueOf(numMessages - count) * 100 / numMessages);
					System.out.println(
							String.format("Remaining: %d on total %d, complete %s%%", count, numMessages, percentage));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}, "Monitor");

		System.out.println("Start sending and monitoring....");

		startSignal.countDown();
		monitorThread.start();

		long startTime = System.nanoTime();
		doneSignal.await();
		long time = System.nanoTime() - startTime;

		System.out.println("Got all message acks, interupt monitor thread and generate report...");

		monitorThread.interrupt();

		long totalDataSentAndReceive = (long) messageSize * (long) numMessages * 2l;
		double totalDataSentAndReceiveMB = totalDataSentAndReceive / (1024 * 1024);

		System.out.println(String.format("************ REPORT ************"));
		System.out.println(String.format("Num messages: %d", numMessages));
		System.out.println(String.format("Number of threads: %d", numThreads));
		System.out.println(String.format("Executing time: %s sec", df.format(Double.valueOf(time / 1e9))));
		System.out.println(String.format("Message size: %dbyte = %skb", messageSize,
				df.format(Double.valueOf(messageSize) / 1024)));
		System.out.println(String.format("Total data (sent + received): %sMb (%sGb)",
				df.format(Double.valueOf(totalDataSentAndReceiveMB)),
				df.format(Double.valueOf(totalDataSentAndReceiveMB / 1014))));
		System.out.println(String.format("Throughput (sent + received): %sMb/s",
				df.format(Double.valueOf(totalDataSentAndReceiveMB) / Double.valueOf(time / 1e9))));
		System.out.println(String.format("Message per second: %s",
				df.format(Double.valueOf(numMessages) / Double.valueOf(time / 1e9))));

		System.exit(0);
	}

	public void onConnectedHandler(Event event) {
		System.out.println("Connected to server at " + this.getServerAddress() + " --> start sending");
		new Thread() {
			public void run() {
				try {
					send();
				} catch (Exception e) {
					getLogger().error("Send ping error", e);
					System.exit(1);
				}
			}
		}.start();
	}

	public void onDisconnectedHandler(Event event) {
		System.out.println("Disconnected to server at " + this.getServerAddress());
		System.exit(0);
	}
}
