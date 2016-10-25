package nhb.mario3.producer;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import nhb.common.BaseLoggable;
import nhb.common.exception.UnsupportedTypeException;
import nhb.common.utils.FileSystemUtils;
import nhb.mario3.config.HttpMessageProducerConfig;
import nhb.mario3.config.KafkaMessageProducerConfig;
import nhb.mario3.config.MessageProducerConfig;
import nhb.mario3.config.RabbitMQProducerConfig;
import nhb.mario3.gateway.rabbitmq.RabbitMQServerWrapper;
import nhb.mario3.gateway.serverwrapper.ServerWrapper;
import nhb.mario3.gateway.serverwrapper.ServerWrapperManager;
import nhb.messaging.MessageProducer;
import nhb.messaging.http.producer.HttpAsyncMessageProducer;
import nhb.messaging.http.producer.HttpMessageProducer;
import nhb.messaging.http.producer.HttpSyncMessageProducer;
import nhb.messaging.kafka.producer.KafkaMessageProducer;
import nhb.messaging.rabbit.connection.RabbitMQConnection;
import nhb.messaging.rabbit.producer.RabbitMQProducer;
import nhb.messaging.rabbit.producer.RabbitMQRPCProducer;
import nhb.messaging.rabbit.producer.RabbitMQRoutingProducer;
import nhb.messaging.rabbit.producer.RabbitMQRoutingRPCProducer;
import nhb.messaging.rabbit.producer.RabbitMQTaskProducer;

public class MessageProducerManager extends BaseLoggable {

	private ServerWrapperManager serverWrapperManager;
	private Map<String, MessageProducer<?>> producers = new ConcurrentHashMap<>();

	public MessageProducerManager(ServerWrapperManager serverWrapperManager) {
		this.serverWrapperManager = serverWrapperManager;
	}

	public void init(Collection<MessageProducerConfig> configs) {
		this.producers = new ConcurrentHashMap<>();
		if (configs != null) {
			configs.forEach(config -> {
				if (config != null) {
					if (config instanceof RabbitMQProducerConfig) {
						initRabbitMQProducer((RabbitMQProducerConfig) config);
					} else if (config instanceof HttpMessageProducerConfig) {
						initHttpMessageProducer((HttpMessageProducerConfig) config);
					} else if (config instanceof KafkaMessageProducerConfig) {
						initKafkaMessageProducer((KafkaMessageProducerConfig) config);
					} else {
						throw new UnsupportedTypeException();
					}
				}

			});
		}
	}

	private void initKafkaMessageProducer(KafkaMessageProducerConfig config) {
		String extensionsFolder = System.getProperty("application.extensionsFolder", "extensions");
		String extensionName = config.getExtensionName();
		String configFile = config.getConfigFile();
		String filePath = FileSystemUtils.createAbsolutePathFrom(extensionsFolder, extensionName, configFile);
		try (InputStream is = new FileInputStream(filePath)) {
			Properties props = new Properties();
			props.load(is);
			KafkaMessageProducer producer = new KafkaMessageProducer(props, config.getTopic());
			this.producers.put(config.getName(), producer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private RabbitMQConnection getConnection(String name) {
		ServerWrapper serverWrapper = this.serverWrapperManager.getServerWrapper(name);
		if (serverWrapper instanceof RabbitMQServerWrapper) {
			return ((RabbitMQServerWrapper) serverWrapper).getConnection();
		}
		return null;
	}

	private void initHttpMessageProducer(HttpMessageProducerConfig config) {
		HttpMessageProducer<?> producer = config.isAsync() ? new HttpAsyncMessageProducer()
				: new HttpSyncMessageProducer();
		producer.setEndpoint(config.getEndpoint());
		producer.setMethod(config.getHttpMethod());
		producer.setUsingMultipath(config.isUsingMultipart());

		this.producers.put(config.getName(), producer);
	}

	private void initRabbitMQProducer(RabbitMQProducerConfig config) {

		MessageProducer<?> producer = null;

		switch (config.getQueueConfig().getType()) {
		case ROUTING:
			producer = new RabbitMQRoutingProducer(this.getConnection(config.getConnectionName()),
					config.getQueueConfig());
			break;
		case ROUTING_RPC:
			producer = new RabbitMQRoutingRPCProducer(this.getConnection(config.getConnectionName()),
					config.getQueueConfig());
			break;
		case RPC:
			producer = new RabbitMQRPCProducer(this.getConnection(config.getConnectionName()), config.getQueueConfig());
			break;
		case TASK_QUEUE:
			producer = new RabbitMQTaskProducer(this.getConnection(config.getConnectionName()),
					config.getQueueConfig());
			break;
		case PUB_SUB:
		case TOPIC:
		default:
			throw new UnsupportedTypeException();
		}

		if (producer != null) {
			this.producers.put(config.getName(), producer);
		}
	}

	public void start() {
		this.producers.values().stream().filter(producer -> {
			return producer instanceof RabbitMQProducer;
		}).forEach(messageProducer -> {
			((RabbitMQProducer<?>) messageProducer).start();
		});
	}

	@SuppressWarnings("unchecked")
	public <T extends MessageProducer<?>> T getProducer(String name) {
		return (T) this.producers.get(name);
	}

	public void stop() {
		this.producers.values().stream().filter(producer -> {
			return producer instanceof Closeable;
		}).forEach(messageProducer -> {
			try {
				((Closeable) messageProducer).close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
