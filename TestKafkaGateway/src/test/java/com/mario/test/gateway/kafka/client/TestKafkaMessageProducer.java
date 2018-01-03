package com.mario.test.gateway.kafka.client;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import nhb.common.BaseLoggable;
import nhb.common.data.MapTuple;
import nhb.common.data.PuObject;
import nhb.common.utils.Converter;
import nhb.common.utils.FileSystemUtils;
import nhb.common.utils.Initializer;
import nhb.messaging.kafka.producer.KafkaMessageProducer;

public class TestKafkaMessageProducer extends BaseLoggable implements Closeable {

	private KafkaMessageProducer producer;

	static {
		Initializer.bootstrap(TestKafkaMessageProducer.class);
	}

	public static void main(String[] args) throws IOException {
		try (TestKafkaMessageProducer app = new TestKafkaMessageProducer()) {
			app.run();
		}
	}

	private TestKafkaMessageProducer() throws IOException {
		Properties properties = new Properties();
		try (InputStream is = new FileInputStream(
				FileSystemUtils.createAbsolutePathFrom("conf", "producer.properties"))) {
			properties.load(is);
		}
		String defaultTopic = "test.topic1";
		producer = new KafkaMessageProducer(properties, defaultTopic);
	}

	private void run() {
		for (int i = 0; i < 100; i++) {
			byte[] key = producer.publish(PuObject.fromObject(new MapTuple<>("value", i)));
			getLogger().debug("published message with key: " + Converter.bytesToUUID(key) + " at id: " + i);
		}
	}

	@Override
	public void close() {
		if (this.producer != null) {
			this.producer.stop();
		}
	}
}
