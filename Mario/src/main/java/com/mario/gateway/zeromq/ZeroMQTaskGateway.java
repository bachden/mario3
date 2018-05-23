package com.mario.gateway.zeromq;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.mario.config.WorkerPoolConfig;
import com.mario.entity.MessageHandleCallback;
import com.mario.entity.message.Message;
import com.mario.gateway.GatewayEvent;
import com.mario.gateway.zeromq.metadata.ZeroMQInputMetadataProcessor;
import com.mario.worker.MessageHandlingWorker;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.exception.InvalidDataException;
import com.nhb.common.data.msgpkg.PuElementTemplate;
import com.nhb.messaging.zmq.ZMQSocket;
import com.nhb.messaging.zmq.ZMQSocketType;

import lombok.AccessLevel;
import lombok.Getter;

public class ZeroMQTaskGateway extends ZeroMQGateway {

	private Disruptor<ZeroMQMessage> disruptor;
	private final AtomicBoolean running = new AtomicBoolean(false);
	private Thread pollingThread;
	private ZMQSocket socket;

	private final ExceptionHandler<ZeroMQMessage> exceptionHandler = new ExceptionHandler<ZeroMQMessage>() {

		@Override
		public void handleOnStartException(Throwable ex) {
			getLogger().error("Error while start disruptor: ", ex);
		}

		@Override
		public void handleOnShutdownException(Throwable ex) {
			getLogger().error("Error while shutdown disruptor: ", ex);
		}

		@Override
		public void handleEventException(Throwable ex, long sequence, ZeroMQMessage event) {
			onHandleError(event, ex);
		}
	};

	@Getter(AccessLevel.PROTECTED)
	private ThreadFactory threadFactory;

	private static class Unmarshaller implements WorkHandler<ZeroMQMessage> {

		@Override
		public void onEvent(ZeroMQMessage event) throws Exception {
			PuElement data = PuElementTemplate.getInstance().read(event.getRawInput());
			if (!(data instanceof PuArray)) {
				throw new InvalidDataException("ZeroMQGateway expected for only PuArray data, got: " + data.getClass());
			}
			PuArray arr = (PuArray) data;
			if (arr.size() > 1) {
				if (event.getMetadataProcessor() != null) {
					PuArray metadata = arr.getPuArray(0);
					event.getMetadataProcessor().processInputMetadata(metadata, event);
				}

				PuValue puValue = arr.get(1);
				if (puValue.getType() == PuDataType.PUARRAY) {
					event.setData(puValue.getPuArray());
				} else if (puValue.getType() == PuDataType.PUOBJECT) {
					event.setData(puValue.getPuObject());
				} else {
					event.setData(puValue);
				}
			} else {
				throw new InvalidDataException(
						"ZeroMQGateway expected for only PuArray data, and must have exact 2 elements for message metadata and body");
			}
		}
	}

	public boolean isRunning() {
		return this.running.get();
	}

	@Override
	public void processInputMetadata(PuArray metadata, ZeroMQMessage message) {
		// do nothing
	}

	@Override
	public void onHandleComplete(Message message, PuElement result) {
		// do nothing...
	}

	@Override
	public void onHandleError(Message message, Throwable exception) {
		getLogger().error("Error while handle message: {}", message, exception);
	}

	@Override
	protected void _init() {

		WorkerPoolConfig workerPoolConfig = getConfig().getWorkerPoolConfig();

		int ringBufferSize = workerPoolConfig.getRingBufferSize();
		String threadNamePattern = workerPoolConfig.getThreadNamePattern();

		threadFactory = new ThreadFactoryBuilder().setNameFormat(threadNamePattern).build();

		Unmarshaller[] unmarshallers = new Unmarshaller[workerPoolConfig.getUnmarshallerSize()];
		for (int i = 0; i < unmarshallers.length; i++) {
			unmarshallers[i] = new Unmarshaller();
		}

		MessageHandlingWorker[] handlers = new MessageHandlingWorker[workerPoolConfig.getPoolSize()];
		for (int i = 0; i < handlers.length; i++) {
			handlers[i] = new MessageHandlingWorker();
			handlers[i].setCallback(this);
			handlers[i].setHandler(this.getHandler());
		}

		this.disruptor = new Disruptor<ZeroMQMessage>(ZeroMQMessage.EVENT_FACTORY, ringBufferSize, threadFactory,
				ProducerType.SINGLE, workerPoolConfig.getWaitStrategy());
		this.disruptor.handleEventsWithWorkerPool(unmarshallers).thenHandleEventsWithWorkerPool(handlers);
		this.disruptor.setDefaultExceptionHandler(exceptionHandler);

		this.__init();
	}

	protected void __init() {
		// to be overrided by sub class
	}

	protected void processSocketBeforeStart(ZMQSocket socket) {
		// do nothing
	}

	@Override
	public final void start() throws Exception {
		if (this.running.compareAndSet(false, true)) {
			socket = this.getZMQSocketRegistry().openSocket(this.getConfig().getEndpoint(), ZMQSocketType.PULL_BIND);
			this.processSocketBeforeStart(socket);

			final MessageHandleCallback callback = this;
			final ZeroMQInputMetadataProcessor metadataProcessor = this;
			this.pollingThread = new Thread() {

				@Override
				public void run() {
					while (running.get()) {
						final byte[] data = socket.recv();
						disruptor.publishEvent(new EventTranslator<ZeroMQMessage>() {

							@Override
							public void translateTo(ZeroMQMessage event, long sequence) {
								event.setRawInput(data);
								event.setCallback(callback);
								event.setMetadataProcessor(metadataProcessor);
							}
						});
					}
				};
			};

			// continue starting progress
			this._start();

			// start handling
			this.disruptor.start();
			this.pollingThread.start();

			// dispatch started event
			this.dispatchEvent(GatewayEvent.createStartedEvent());
		}
	}

	protected void _start() {
		// to be overrided by sub class
	}

	@Override
	public final void stop() throws Exception {
		if (this.running.compareAndSet(true, false)) {
			try {
				this.socket.close();
				this.socket = null;
			} catch (Exception e) {
				System.err.println("Error while closing socket from gateway " + this.getName());
				e.printStackTrace();
			}

			try {
				this.disruptor.halt();
				this.disruptor = null;
			} catch (Exception e) {
				System.err.println("Error while shutdown threads, gateway name: " + this.getName());
				e.printStackTrace();
			}

			try {
				this.pollingThread.interrupt();
				this.pollingThread = null;
			} catch (Exception e) {
				System.err.println("Error while interupting polling thread, gateway name: " + this.getName());
			}

			this._stop();
		}
	}

	protected void _stop() {
		// to be overrided by sub class
	}
}
