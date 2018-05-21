package com.mario.gateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.mario.config.gateway.GatewayConfig;
import com.mario.config.gateway.GatewayType;
import com.mario.config.gateway.KafkaGatewayConfig;
import com.mario.config.gateway.RabbitMQGatewayConfig;
import com.mario.config.gateway.SocketGatewayConfig;
import com.mario.config.gateway.ZeroMQGatewayConfig;
import com.mario.entity.ExtensionNameAware;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.mario.entity.message.transcoder.MessageEncoder;
import com.mario.extension.ExtensionManager;
import com.mario.gateway.http.HttpGateway;
import com.mario.gateway.kafka.KafkaGatewayFactory;
import com.mario.gateway.rabbitmq.RabbitMQGatewayFactory;
import com.mario.gateway.serverwrapper.HasServerWrapper;
import com.mario.gateway.serverwrapper.ServerWrapper;
import com.mario.gateway.serverwrapper.ServerWrapperManager;
import com.mario.gateway.socket.SocketGateway;
import com.mario.gateway.socket.SocketGatewayFactory;
import com.mario.gateway.socket.SocketSessionManager;
import com.mario.gateway.zeromq.ZeroMQGatewayFactory;
import com.mario.ssl.SSLContextManager;
import com.nhb.common.BaseLoggable;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventHandler;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

public final class GatewayManager extends BaseEventDispatcher {

	private static class GatewayFactory extends BaseLoggable {
		public Gateway newGateway(GatewayConfig config) {
			if (config != null) {
				GatewayType type = config.getType();
				Gateway result = null;
				switch (type) {
				case HTTP:
					result = new HttpGateway();
					break;
				case RABBITMQ:
					result = RabbitMQGatewayFactory.newRabbitGateway(((RabbitMQGatewayConfig) config));
					break;
				case SOCKET:
					result = SocketGatewayFactory.newSocketGateway(((SocketGatewayConfig) config));
					break;
				case KAFKA:
					result = KafkaGatewayFactory.newKafkaGateway((KafkaGatewayConfig) config);
					break;
				case ZEROMQ:
					result = ZeroMQGatewayFactory.newZeroMQGateway((ZeroMQGatewayConfig) config);
					break;
				default:
					break;
				}
				return result;
			}
			return null;
		}
	}

	private final GatewayFactory gatewayFactory = new GatewayFactory();
	private final Map<String, Gateway> gatewayByName = new HashMap<String, Gateway>();
	private ExecutorService gatewayThreadPool;
	private ExtensionManager extensionManager;
	private SocketSessionManager socketSessionManager;
	private ServerWrapperManager serverWrapperManager;
	private SSLContextManager sslContextManager;

	public GatewayManager(ExtensionManager extensionManager, ServerWrapperManager serverWrapperManager,
			SSLContextManager sslContextManager) {
		this.extensionManager = extensionManager;
		this.serverWrapperManager = serverWrapperManager;
		this.sslContextManager = sslContextManager;
		this.socketSessionManager = new SocketSessionManager();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void init(List<GatewayConfig> gatewayConfigs)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		if (gatewayConfigs == null || gatewayConfigs.size() == 0) {
			getLogger().warn("no gateway config found");
			return;
		}

		for (GatewayConfig config : gatewayConfigs) {
			if (config.getName() == null || config.getName().trim().length() == 0) {
				throw new NullPointerException("Cannot init gateway with empty name");
			}

			Gateway gateway = this.gatewayFactory.newGateway(config);
			if (gateway instanceof SSLContextAware) {
				String sslContextName = config.getSSLContextName();
				if (sslContextName != null) {
					((SSLContextAware) gateway).setSSLContext(this.sslContextManager.getSSLContext(sslContextName));
				}
			}

			if (gateway instanceof ExtensionNameAware) {
				((ExtensionNameAware) gateway).setExtensionName(config.getExtensionName());
			}

			if (gateway instanceof HasDeserializerGateway) {
				if (config.getDeserializerClassName() == null) {
					if (((HasDeserializerGateway) gateway).isDeserializerRequired()) {
						throw new NullPointerException(
								"Cannot init gateway '" + config.getName() + "' without deserialize class name");
					}
				} else {
					MessageDecoder deserializer = this.extensionManager.newInstance(config.getExtensionName(),
							config.getDeserializerClassName().trim());

					((HasDeserializerGateway) gateway).setDeserializer(deserializer);
				}
			}

			if (gateway instanceof HasSerializerGateway) {
				if (config.getSerializerClassName() == null) {
					if (((HasSerializerGateway) gateway).isSerializerRequired()) {
						throw new NullPointerException(
								"Cannot init gateway '" + config.getName() + "' without serialize class name");
					}
				} else {
					MessageEncoder serializer = this.extensionManager.newInstance(config.getExtensionName(),
							config.getSerializerClassName().trim());
					((HasSerializerGateway) gateway).setSerializer(serializer);
				}
			}

			if (gateway instanceof HasServerWrapper) {
				String serverWrapperName = config.getServerWrapperName();
				if (serverWrapperName == null) {
					throw new NullPointerException(
							"Gateway " + config.getName() + " require server wrapper which is not exists in config");
				}

				ServerWrapper serverWrapper = this.serverWrapperManager.getServerWrapper(serverWrapperName);
				if (serverWrapper == null) {
					throw new NullPointerException(
							"Cannot init gateway " + config.getName() + " without server wrapper named "
									+ config.getServerWrapperName() + " (which may not exist)");
				}

				((HasServerWrapper) gateway).setServer(serverWrapper);
			}

			if (gateway instanceof SocketGateway) {
				((SocketGateway) gateway).setSessionManager(this.socketSessionManager);
			}

			gateway.init(config);
			this.gatewayByName.put(config.getName(), gateway);
		}
	}

	public void start() {
		if (gatewayByName.size() > 0) {
			this.gatewayThreadPool = new ThreadPoolExecutor(this.gatewayByName.size(), this.gatewayByName.size(), 60,
					TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

						final AtomicInteger threadNumber = new AtomicInteger(1);

						@Override
						public Thread newThread(Runnable r) {
							return new Thread(r, String.format("Gateway Starter #%d", threadNumber.getAndIncrement()));
						}
					});

			AtomicInteger startedGatewayCount = new AtomicInteger(0);
			final EventHandler gatewayStartedListener = new EventHandler() {

				@Override
				public void onEvent(Event event) throws Exception {
					GatewayEvent gatewayEvent = (GatewayEvent) event;
					gatewayEvent.getTarget().removeEventListener(GatewayEvent.STARTED, this);
					if (startedGatewayCount.incrementAndGet() == gatewayByName.size()) {
						GatewayManager.this.dispatchEvent(GatewayEvent.createAllGatewayReadyEvent());
					}
				}
			};

			// parallel starting all the gateways
			this.gatewayByName.values().forEach(gateway -> {

				this.gatewayThreadPool.execute(new Runnable() {

					@Override
					public void run() {
						try {
							gateway.addEventListener(GatewayEvent.STARTED, gatewayStartedListener);
							gateway.start();
						} catch (Exception e) {
							getLogger().error("cannot start gateway {}", gateway.getName(), e);
						}
					}
				});
			});
		} else {
			getLogger().warn("no gateway to start");
			GatewayManager.this.dispatchEvent(GatewayEvent.createAllGatewayReadyEvent());
		}
	}

	public void stop() {
		for (Gateway gateway : this.gatewayByName.values()) {
			try {
				System.out.println("Stopping gateway " + gateway.getName());
				gateway.stop();
			} catch (Exception e) {
				getLogger().error("Cannot stop gateway: " + gateway.getName(), e);
				System.err.println("Cannot stop gateway: " + gateway.getName());
				e.printStackTrace();
			}
		}
		List<String> gatewayNames = new ArrayList<>(this.gatewayByName.keySet());
		for (String name : gatewayNames) {
			this.gatewayByName.remove(name);
		}

		if (gatewayThreadPool != null) {
			this.gatewayThreadPool.shutdown();
			try {
				if (this.gatewayThreadPool.awaitTermination(3, TimeUnit.SECONDS)) {
					System.err.println("cannot shutdown gateway threadpool, force by calling shutdownNow() method");
					this.gatewayThreadPool.shutdownNow();
					if (this.gatewayThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
						System.err.println("cannot shutdown gateway threadpool...");
					}
				}
			} catch (InterruptedException ex) {
				getLogger().error("error while waiting for gateway thread pool to shutdown", ex);
				System.err.println("error while waiting for gateway thread pool to shutdown");
				ex.printStackTrace();
			}
		}
	}

	public Gateway getGatewayByName(String name) {
		if (name != null && name.trim().length() > 0) {
			return this.gatewayByName.get(name.trim());
		}
		return null;
	}

	public Collection<Gateway> getGateways() {
		return this.gatewayByName.values();
	}

	public SocketSessionManager getSocketSessionManager() {
		return socketSessionManager;
	}
}
