package com.mario.test.gateway.kafka.client;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuObject;
import com.nhb.common.utils.UUIDUtils;
import com.nhb.messaging.kafka.producer.KafkaMessageProducer;

public class TestKafkaMessageProducer extends BaseLoggable implements Closeable {

	private KafkaMessageProducer producer;

	public static void main(String[] args) throws IOException {
		try (TestKafkaMessageProducer app = new TestKafkaMessageProducer()) {
			app.run();
		}
	}

	private TestKafkaMessageProducer() throws IOException {
		Properties properties = new Properties();
		try (InputStream is = new FileInputStream(new File("conf", "producer.properties"))) {
			properties.load(is);
		}
		String defaultTopic = "test.topic1";
		producer = new KafkaMessageProducer(properties, defaultTopic);
	}

	private void run() {
		for (int i = 0; i < 100; i++) {
			byte[] key = producer.publish(PuObject.fromObject(new MapTuple<>("value", i)));
			getLogger().debug("published message with key: " + UUIDUtils.bytesToUUID(key) + " at id: " + i);
		}
	}

	@Override
	public void close() {
		if (this.producer != null) {
			this.producer.stop();
		}
	}
}
