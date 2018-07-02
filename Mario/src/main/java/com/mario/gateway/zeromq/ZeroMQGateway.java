package com.mario.gateway.zeromq;

import java.util.concurrent.atomic.AtomicBoolean;

import com.mario.Mario;
import com.mario.config.gateway.GatewayConfig;
import com.mario.config.gateway.GatewayType;
import com.mario.config.gateway.ZeroMQGatewayConfig;
import com.mario.entity.MessageHandler;
import com.mario.entity.message.Message;
import com.mario.gateway.Gateway;
import com.mario.gateway.GatewayEvent;
import com.mario.zeromq.ZMQSocketRegistryManager;
import com.nhb.common.async.CompletableFuture;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;
import com.nhb.eventdriven.impl.BaseEventDispatcher;
import com.nhb.messaging.zmq.ZMQSocketOptions;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;
import com.nhb.messaging.zmq.ZMQSocketWriter;
import com.nhb.messaging.zmq.consumer.ZMQConsumer;
import com.nhb.messaging.zmq.consumer.ZMQConsumerConfig;
import com.nhb.messaging.zmq.consumer.ZMQMessageProcessor;
import com.nhb.messaging.zmq.consumer.ZMQRPCConsumer;
import com.nhb.messaging.zmq.consumer.ZMQTaskConsumer;

import lombok.Getter;
import lombok.Setter;

public class ZeroMQGateway extends BaseEventDispatcher implements Gateway, ZMQMessageProcessor {

	@Getter
	private String name;

	@Setter
	@Getter
	private MessageHandler handler;

	@Getter
	private final ZeroMQGatewayConfig config;

	@Getter
	private boolean initialized = false;
	private final AtomicBoolean initCheckpoint = new AtomicBoolean(false);

	private ZMQConsumer consumer;

	public ZeroMQGateway(GatewayConfig config) {
		this.config = (ZeroMQGatewayConfig) config;
		this.name = config.getName();

		if (this.config.getZeroMQGatewayType() != null) {
			switch (this.config.getZeroMQGatewayType()) {
			case RPC:
				this.consumer = new ZMQRPCConsumer();
				break;
			case SUB:
			case TASK:
				this.consumer = new ZMQTaskConsumer();
				break;
			default:
				break;
			}
		}
		if (consumer == null) {
			throw new IllegalArgumentException("ZMQ gateway type invalid: " + this.config.getZeroMQGatewayType());
		}
	}

	@Override
	public final void init(GatewayConfig config) {
		if (initCheckpoint.compareAndSet(false, true)) {
			this.consumer.init(this.generateConsumerConfig());
			this.initialized = true;
		}
	}

	private ZMQConsumerConfig generateConsumerConfig() {
		long hwm = this.config.getHwm();
		ZMQSocketRegistryManager zmqSocketRegistryManager = Mario.getInstance().getZmqSocketRegistryManager();
		ZMQSocketRegistry socketRegistry = zmqSocketRegistryManager.getZMQSocketRegistry(this.config.getRegistryName());
		ZMQSocketWriter socketWriter = ZMQSocketWriter.newNonBlockingWriter(this.config.getMessageBufferSize());

		ZMQSocketOptions socketOptions = ZMQSocketOptions.builder() //
				.hwm(hwm) //
				.sndHWM(hwm) //
				.rcvHWM(hwm) //
				.topics(this.config.getListSubKeys()) //
				.build();

		ZMQConsumerConfig config = new ZMQConsumerConfig();
		config.setSendSocketOptions(socketOptions);
		config.setSendWorkerSize(this.config.getNumSenders());
		config.setReceiveEndpoint(this.config.getEndpoint());
		config.setBufferCapacity(this.config.getBufferCapacity());
		config.setSocketWriter(socketWriter);
		config.setSocketRegistry(socketRegistry);
		config.setMessageProcessor(this);
		config.setQueueSize(this.config.getQueueSize());
		config.setReceiveWorkerSize(this.config.getNumHandlers());
		config.setThreadNamePattern(this.config.getThreadNamePattern());
		config.setReceivedCountEnabled(this.config.isReceivedCountEnabled());
		config.setRespondedCountEnabled(this.config.isRespondedCountEnabled());

		switch (this.config.getZeroMQGatewayType()) {
		case RPC:
		case TASK:
			config.setReceiveSocketType(ZMQSocketType.PULL_BIND);
			break;
		case SUB:
			config.setReceiveSocketType(ZMQSocketType.SUB_BIND);
			break;
		}

		return config;
	}

	@Getter
	private boolean running = false;
	private final AtomicBoolean runningCheckpoint = new AtomicBoolean(false);

	@Override
	public void start() throws Exception {
		if (runningCheckpoint.compareAndSet(false, true)) {
			getLogger().info("Starting ZeroMQGateway name {} at endpoint {}", this.getName(),
					this.config.getEndpoint());
			this.dispatchEvent(GatewayEvent.createBeforeStartEvent());
			this.consumer.start();
			this.running = true;
			this.dispatchEvent(GatewayEvent.createStartedEvent());
		}
	}

	@Override
	public void stop() throws Exception {
		if (this.runningCheckpoint.compareAndSet(true, false)) {
			this.consumer.stop();
			this.running = false;
		}
	}

	@Override
	public void process(PuElement data, CompletableFuture<PuElement> future) {
		if (this.handler == null) {
			future.setFailedCause(new NullPointerException("No handle to process request"));
			future.setAndDone(null);
		} else {
			ZeroMQMessage message = new ZeroMQMessage();
			message.setData(data);
			message.setCallback(this);
			message.setGatewayName(this.getName());
			message.setFuture(future);
			message.setGatewayType(GatewayType.ZEROMQ);

			try {
				PuElement result = this.handler.handle(message);
				if (!(result instanceof PuNull)) {
					this.onHandleComplete(message, result);
				}
			} catch (Exception e) {
				this.onHandleError(message, e);
			}
		}
	}

	@Override
	public void onHandleComplete(Message message, PuElement result) {
		if (message instanceof ZeroMQMessage) {
			ZeroMQMessage zmqMessage = (ZeroMQMessage) message;
			CompletableFuture<PuElement> future = zmqMessage.getFuture();
			if (future != null) {
				future.setAndDone(result);
			}
		}
	}

	@Override
	public void onHandleError(Message message, Throwable exception) {
		if (message instanceof ZeroMQMessage) {
			ZeroMQMessage zmqMessage = (ZeroMQMessage) message;
			CompletableFuture<PuElement> future = zmqMessage.getFuture();
			if (future != null) {
				future.setFailedCause(exception);
				future.setAndDone(null);
			}
		}
	}

	public long getReceivedCount() {
		return this.consumer.getReceivedCount();
	}

	public long getRespondedCount() {
		if (this.consumer instanceof ZMQRPCConsumer) {
			return ((ZMQRPCConsumer) this.consumer).getRespondedCount();
		}
		throw new UnsupportedOperationException("Responded count only supported in RPC gateway");
	}

}
