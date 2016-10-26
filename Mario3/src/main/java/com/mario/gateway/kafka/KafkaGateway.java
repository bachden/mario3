package com.mario.gateway.kafka;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.mario.config.gateway.GatewayType;
import com.mario.config.gateway.KafkaGatewayConfig;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.BaseKafkaMessage;
import com.mario.gateway.AbstractGateway;
import com.mario.worker.MessageEventFactory;
import com.nhb.common.data.PuElement;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventHandler;
import com.nhb.messaging.kafka.consumer.KafkaMessageConsumer;
import com.nhb.messaging.kafka.event.KafkaEvent;

public class KafkaGateway extends AbstractGateway<KafkaGatewayConfig> {

	private String defaultConfigFile = FileSystemUtils.createAbsolutePathFrom("conf",
			"kafka-default-consumer.properties");

	private KafkaMessageConsumer consumer;

	private EventHandler eventHandler = new EventHandler() {

		@Override
		public void onEvent(Event event) throws Exception {
			KafkaEvent kafkaEvent = (KafkaEvent) event;
			ConsumerRecord<byte[], PuElement> record = kafkaEvent.getRecord();
			publishToWorkers(record);
		}
	};

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
		this.consumer.addEventListener(KafkaEvent.NEW_RECORD, this.eventHandler);
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
