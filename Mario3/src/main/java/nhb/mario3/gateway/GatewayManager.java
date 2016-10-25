package nhb.mario3.gateway;

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

import nhb.common.BaseLoggable;
import nhb.eventdriven.Event;
import nhb.eventdriven.EventHandler;
import nhb.eventdriven.impl.BaseEventDispatcher;
import nhb.mario3.config.gateway.GatewayConfig;
import nhb.mario3.config.gateway.GatewayType;
import nhb.mario3.config.gateway.KafkaGatewayConfig;
import nhb.mario3.config.gateway.RabbitMQGatewayConfig;
import nhb.mario3.config.gateway.SocketGatewayConfig;
import nhb.mario3.entity.message.transcoder.MessageDecoder;
import nhb.mario3.entity.message.transcoder.MessageEncoder;
import nhb.mario3.extension.ExtensionManager;
import nhb.mario3.gateway.http.HttpGateway;
import nhb.mario3.gateway.kafka.KafkaGatewayFactory;
import nhb.mario3.gateway.rabbitmq.RabbitMQGatewayFactory;
import nhb.mario3.gateway.serverwrapper.HasServerWrapper;
import nhb.mario3.gateway.serverwrapper.ServerWrapperManager;
import nhb.mario3.gateway.socket.SocketGateway;
import nhb.mario3.gateway.socket.SocketGatewayFactory;
import nhb.mario3.gateway.socket.SocketSessionManager;

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

	public GatewayManager(ExtensionManager extensionManager, ServerWrapperManager serverWrapperManager) {
		this.extensionManager = extensionManager;
		this.serverWrapperManager = serverWrapperManager;
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
				getLogger().error("cannot init gateway with empty name", new Exception());
				continue;
			}
			Gateway gateway = this.gatewayFactory.newGateway(config);
			if (gateway instanceof AbstractGateway) {
				((AbstractGateway<?>) gateway).setExtensionName(config.getExtensionName());
				if (config.getDeserializerClassName() != null) {
					MessageDecoder deserializer = this.extensionManager.newInstance(config.getExtensionName(),
							config.getDeserializerClassName().trim());
					((AbstractGateway<?>) gateway).setDeserializer(deserializer);
				} else {
					throw new RuntimeException("cannot init gateway without MessageDeserializer");
				}
				if (config.getSerializerClassName() != null) {
					MessageEncoder serializer = this.extensionManager.newInstance(config.getExtensionName(),
							config.getSerializerClassName().trim());
					((AbstractGateway<?>) gateway).setSerializer(serializer);
				}
				if (gateway instanceof HasServerWrapper) {
					((HasServerWrapper) gateway)
							.setServer(this.serverWrapperManager.getServerWrapper(config.getServerWrapperName()));
				}
				if (gateway instanceof SocketGateway) {
					((SocketGateway) gateway).setSessionManager(this.socketSessionManager);
				}
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
