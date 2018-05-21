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

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractGateway<ConfigType extends GatewayConfig> extends BaseEventDispatcher
		implements Gateway, HasDeserializerGateway, HasSerializerGateway {

	@Setter
	@Getter
	private String name;

	@Setter
	@Getter
	private String extensionName;

	@Getter
	private boolean initialized = false;

	@Setter
	@Getter
	private MessageDecoder deserializer = null;

	@Setter
	@Getter
	private MessageEncoder serializer = null;

	@Getter
	private ConfigType config;

	@Getter
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
