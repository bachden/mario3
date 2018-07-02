package com.mario.producer;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.mario.Mario;
import com.mario.config.HttpMessageProducerConfig;
import com.mario.config.KafkaMessageProducerConfig;
import com.mario.config.MessageProducerConfig;
import com.mario.config.RabbitMQProducerConfig;
import com.mario.config.ZeroMQProducerConfig;
import com.mario.gateway.rabbitmq.RabbitMQServerWrapper;
import com.mario.gateway.serverwrapper.ServerWrapper;
import com.mario.gateway.serverwrapper.ServerWrapperManager;
import com.nhb.common.BaseLoggable;
import com.nhb.common.exception.UnsupportedTypeException;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.StartableProducer;
import com.nhb.messaging.http.producer.HttpAsyncMessageProducer;
import com.nhb.messaging.http.producer.HttpMessageProducer;
import com.nhb.messaging.http.producer.HttpSyncMessageProducer;
import com.nhb.messaging.kafka.producer.KafkaMessageProducer;
import com.nhb.messaging.rabbit.connection.RabbitMQConnection;
import com.nhb.messaging.rabbit.producer.RabbitMQPubSubProducer;
import com.nhb.messaging.rabbit.producer.RabbitMQRPCProducer;
import com.nhb.messaging.rabbit.producer.RabbitMQRoutingProducer;
import com.nhb.messaging.rabbit.producer.RabbitMQRoutingRPCProducer;
import com.nhb.messaging.rabbit.producer.RabbitMQTaskProducer;
import com.nhb.messaging.zmq.ZMQSocketOptions;
import com.nhb.messaging.zmq.ZMQSocketType;
import com.nhb.messaging.zmq.ZMQSocketWriter;
import com.nhb.messaging.zmq.producer.ZMQProducer;
import com.nhb.messaging.zmq.producer.ZMQProducerConfig;
import com.nhb.messaging.zmq.producer.ZMQRPCProducer;
import com.nhb.messaging.zmq.producer.ZMQTaskProducer;

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
					} else if (config instanceof ZeroMQProducerConfig) {
						initZeroMQProducer((ZeroMQProducerConfig) config);
					} else {
						throw new UnsupportedTypeException();
					}
				}
			});
		}
	}

	private void initZeroMQProducer(ZeroMQProducerConfig config) {
		ZMQProducerConfig _config = new ZMQProducerConfig();
		ZMQProducer producer = null;
		if (config.getType() != null) {
			switch (config.getType()) {
			case PUB:
				producer = new ZMQTaskProducer();
				_config.setSendSocketType(ZMQSocketType.PUB_CONNECT);
				break;
			case TASK:
				producer = new ZMQTaskProducer();
				_config.setSendSocketType(ZMQSocketType.PUSH_CONNECT);
				break;
			case RPC:
				producer = new ZMQRPCProducer();
				_config.setSendSocketType(ZMQSocketType.PUSH_CONNECT);
				break;
			}
		} else {
			throw new IllegalArgumentException("Producer type cannot be null");
		}
		if (producer == null) {
			throw new NullPointerException("Producer null cannot be init");
		}

		_config.setBufferCapacity(config.getBufferCapacity());
		_config.setQueueSize(config.getQueueSize());
		_config.setReceiveEndpoint(config.getReceiveEndpoint());
		_config.setReceiveWorkerSize(config.getReceiveWorkerSize());
		_config.setSendEndpoint(config.getEndpoint());
		_config.setSendWorkerSize(config.getNumSenders());
		_config.setSocketRegistry(
				Mario.getInstance().getZmqSocketRegistryManager().getZMQSocketRegistry(config.getRegistryName()));
		_config.setSocketWriter(ZMQSocketWriter.newNonBlockingWriter(config.getMessageBufferSize()));
		_config.setThreadNamePattern(config.getThreadNamePattern());
		_config.setSendSocketOptions(ZMQSocketOptions.builder().hwm(config.getHwm()).sndHWM(config.getHwm())
				.rcvHWM(config.getHwm()).build());
		_config.setSentCountEnabled(config.isSentCountEnabled());
		_config.setReceivedCountEnable(config.isReceivedCountEnabled());

		producer.setName(config.getName());
		producer.init(_config);

		this.producers.put(config.getName(), producer);
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
			producer = new RabbitMQPubSubProducer(this.getConnection(config.getConnectionName()),
					config.getQueueConfig());

			break;
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
			return producer instanceof StartableProducer;
		}).forEach(messageProducer -> {
			((StartableProducer) messageProducer).start();
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
