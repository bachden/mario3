package com.mario.gateway;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.mario.config.WorkerPoolConfig;
import com.mario.config.gateway.GatewayConfig;
import com.mario.entity.MessageHandler;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.BaseMessage;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.mario.entity.message.transcoder.MessageDecodingException;
import com.mario.entity.message.transcoder.MessageEncoder;
import com.mario.worker.MessageEventFactory;
import com.mario.worker.MessageHandlingWorkerPool;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

public abstract class AbstractGateway<ConfigType extends GatewayConfig> extends BaseEventDispatcher implements Gateway {

	private String name;
	private String extensionName;
	private boolean initialized = false;
	private MessageDecoder deserializer = null;
	private MessageEncoder serializer = null;
	private ConfigType config;

	private MessageHandler handler;
	private MessageEventFactory eventFactory;
	private MessageHandlingWorkerPool workerPool;

	protected abstract void _init();

	protected abstract void _start() throws Exception;

	protected abstract void _stop() throws Exception;

	@Override
	@SuppressWarnings("unchecked")
	public final void init(GatewayConfig config) {

		if (this.initialized) {
			throw new RuntimeException("cannot re-init gateway which has been initialized");
		}

		this.name = config.getName();
		this.config = (ConfigType) config;

		this.eventFactory = this.createEventFactory();
		this.workerPool = this.createWorkerPool();

		this._init();

		this.initialized = true;
	}

	protected MessageEventFactory createEventFactory() {
		return new MessageEventFactory() {
			@Override
			public Message newInstance() {
				BaseMessage result = new BaseMessage();
				result.setGatewayName(getName());
				result.setGatewayType(getConfig().getType());
				return result;
			}
		};
	}

	protected MessageHandlingWorkerPool createWorkerPool() {
		MessageHandlingWorkerPool messageHandlingWorkerPool = new MessageHandlingWorkerPool();
		messageHandlingWorkerPool.setConfig(this.getConfig().getWorkerPoolConfig() == null ? new WorkerPoolConfig()
				: this.getConfig().getWorkerPoolConfig());
		return messageHandlingWorkerPool;
	}

	@Override
	public final void start() throws Exception {
		if (this.workerPool == null) {
			return;
		}
		this.dispatchEvent(GatewayEvent.createBeforeStartEvent());
		this.workerPool.start(this.eventFactory, this.getDeserializer(), getHandler(), this);
		this._start();
		this.dispatchEvent(GatewayEvent.createStartedEvent());
	}

	@Override
	public final void stop() throws Exception {
		if (this.workerPool == null) {
			return;
		}
		this.workerPool.shutdown();
		this._stop();
	}

	public String getName() {
		return name;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	public String getExtensionName() {
		return extensionName;
	}

	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

	public MessageDecoder getDeserializer() {
		return deserializer;
	}

	public void setDeserializer(MessageDecoder deserializer) {
		this.deserializer = deserializer;
	}

	public MessageEncoder getSerializer() {
		return serializer;
	}

	public void setSerializer(MessageEncoder serializer) {
		this.serializer = serializer;
	}

	public ConfigType getConfig() {
		return config;
	}

	protected static boolean validateIpString(String ip) {
		try {
			if (ip == null || ip.isEmpty()) {
				return false;
			}

			String[] parts = ip.split("\\.");
			if (parts.length != 4) {
				return false;
			}

			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}
			if (ip.endsWith(".")) {
				return false;
			}

			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	@Override
	public MessageHandler getHandler() {
		return handler;
	}

	@Override
	public void setHandler(MessageHandler handler) {
		if (handler != null && this.handler != null) {
			throw new IllegalStateException("Unable to re-set handler for a gateway's worker pool, gateway name: "
					+ this.getName() + ", extension: " + this.getExtensionName());
		}
		this.handler = handler;
	}

	protected final Message publishToWorkers(Object data) throws MessageDecodingException {
		return this.workerPool.publish(data);
	}

	protected String getFullStacktrace(Throwable throwable) {
		if (throwable != null) {
			return ExceptionUtils.getFullStackTrace(throwable);
		}
		return null;
	}
}
