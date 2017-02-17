package com.mario.gateway.kafka;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import com.mario.config.gateway.GatewayType;
import com.mario.config.gateway.KafkaGatewayConfig;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.BaseKafkaMessage;
import com.mario.entity.message.transcoder.MessageDecodingException;
import com.mario.gateway.AbstractGateway;
import com.mario.worker.MessageEventFactory;
import com.nhb.common.annotations.NotThreadSafe;
import com.nhb.common.data.PuElement;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventHandler;
import com.nhb.messaging.kafka.consumer.KafkaMessageConsumer;
import com.nhb.messaging.kafka.event.KafkaEvent;

import lombok.Getter;

public class KafkaGateway extends AbstractGateway<KafkaGatewayConfig> {

	private String defaultConfigFile = FileSystemUtils.createAbsolutePathFrom("conf",
			"kafka-default-consumer.properties");

	@Getter
	private KafkaMessageConsumer consumer;

	private ScheduledExecutorService scheduledExecutorService;

	private EventHandler eventHandler;

	protected MessageEventFactory createEventFactory() {
		return new MessageEventFactory() {
			@Override
			public Message newInstance() {
				BaseKafkaMessage baseMessage = new BaseKafkaMessage();
				baseMessage.setGatewayType(GatewayType.KAFKA);
				baseMessage.setGatewayName(getName());
				return baseMessage;
			}
		};
	}

	@Override
	protected void _init() {
		Properties properties = this.getDefaultConsumerConfig();

		Properties customConfig = new Properties();
		String extensionsFolder = System.getProperty("application.extensionsFolder", "extensions");
		String extensionName = this.getConfig().getExtensionName();
		String configFile = this.getConfig().getConfigFile();
		String filePath = FileSystemUtils.createAbsolutePathFrom(extensionsFolder, extensionName, configFile);
		try (InputStream is = new FileInputStream(filePath)) {
			customConfig.load(is);
			for (Object key : customConfig.keySet()) {
				properties.put((String) key, customConfig.getProperty((String) key));
			}
		} catch (IOException e) {
			getLogger().warn("Custom kafka configuration file not found at {}, using default config from system",
					filePath);
		}

		this.scheduledExecutorService = Executors.newScheduledThreadPool(2, new ThreadFactory() {

			private String threadNamePattern = "Kafka Gateway Publisher #%d";
			private AtomicInteger idSeed = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, String.format(threadNamePattern, idSeed.incrementAndGet()));
			}
		});

		this.eventHandler = new EventHandler() {

			private final List<ConsumerRecord<byte[], PuElement>> batch = new CopyOnWriteArrayList<>();
			private ScheduledFuture<?> future = null;

			@Override
			public void onEvent(Event event) throws Exception {
				KafkaEvent kafkaEvent = (KafkaEvent) event;
				ConsumerRecords<byte[], PuElement> records = kafkaEvent.getBatch();
				if (getConfig().getMinBatchingSize() <= 0) {
					for (ConsumerRecord<byte[], PuElement> record : records) {
						publishToWorkers(record);
					}
				} else {
					synchronized (this) {
						for (ConsumerRecord<byte[], PuElement> record : records) {
							this.batch.add(record);
						}
					}
					if (this.batch.size() >= getConfig().getMinBatchingSize()) {
						this.publish();
					}
				}
			}

			@NotThreadSafe
			private Message publish() throws MessageDecodingException {
				try {
					if (future != null) {
						future.cancel(false);
					}
					Message message = null;
					if (this.batch.size() >= 0) {
						List<ConsumerRecord<byte[], PuElement>> data = null;
						synchronized (this) {
							data = new ArrayList<>(this.batch);
							this.batch.clear();
						}
						message = publishToWorkers(data);
					}
					return message;
				} finally {
					if (getConfig().getMinBatchingSize() > 0 && getConfig().getMaxRetentionTime() > 0) {
						future = scheduledExecutorService.schedule(new Runnable() {

							@Override
							public void run() {
								Message message = null;
								try {
									message = publish();
								} catch (MessageDecodingException e) {
									onHandleError(message, e);
								}
							}
						}, getConfig().getMaxRetentionTime(), TimeUnit.MILLISECONDS);
					}
				}
			}
		};

		this.consumer = new KafkaMessageConsumer(properties, this.getConfig().getTopics(),
				this.getConfig().getPollTimeout());

	}

	protected Properties getDefaultConsumerConfig() {
		Properties properties = new Properties();
		try (InputStream is = new FileInputStream(this.defaultConfigFile)) {
			properties.load(is);
		} catch (IOException e) {
			getLogger().info("Default properties file for kafka consumer config was not found");
		}
		return properties;
	}

	@Override
	protected void _start() throws Exception {
		this.consumer.addEventListener(KafkaEvent.NEW_BATCH, this.eventHandler);
		this.consumer.start();
		getLogger().info("Kafka gateway '{}' started successfully", this.getName());
	}

	@Override
	protected void _stop() throws Exception {
		if (this.consumer != null) {
			this.consumer.stop();
		}
	}

	@Override
	public void onHandleComplete(Message message, PuElement result) {
		// do nothing
	}

	@Override
	public void onHandleError(Message message, Throwable exception) {
		getLogger().error("Error while handling message: {}", message.getData(), exception);
	}

}
