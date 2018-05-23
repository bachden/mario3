package com.mario.gateway.zeromq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

import org.zeromq.ZMQ;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.mario.config.WorkerPoolConfig;
import com.mario.entity.message.Message;
import com.mario.gateway.zeromq.metadata.ZeroMQOutputMetadataProcessor;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.exception.InvalidDataException;
import com.nhb.messaging.zmq.ZMQSocket;
import com.nhb.messaging.zmq.ZMQSocketType;

import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ZeroMQRPCGateway extends ZeroMQTaskGateway {

	private final Map<String, ZeroMQResponseWriter> responseWriters = new ConcurrentHashMap<>();

	@Override
	public void onHandleComplete(final Message message, final PuElement result) {
		if (!(message instanceof ZeroMQMessage)) {
			throw new InvalidDataException("Message must be instanceof ZeroMQInputMessage");
		}

		String responseEndpoint = message.cast(ZeroMQMessage.class).getResponseEndpoint();
		ZeroMQResponseWriter writer = null;
		if (!this.responseWriters.containsKey(responseEndpoint)) {
			boolean createNewSuccess = false;
			synchronized (responseWriters) {
				if (!this.responseWriters.containsKey(responseEndpoint)) {
					ZMQSocket socket = this.getZMQSocketRegistry().openSocket(responseEndpoint,
							ZMQSocketType.PUSH_CONNECT);
					writer = new ZeroMQResponseWriter(socket, this.getConfig().getWorkerPoolConfig());
					createNewSuccess = true;
				}
			}
			if (createNewSuccess) {
				writer.start();
			}
		} else {
			writer = this.responseWriters.get(responseEndpoint);
		}

		writer.processResponse((ZeroMQMessage) message, result);
	}

	@Override
	public void processInputMetadata(PuArray metadata, ZeroMQMessage message) {
		if (metadata != null) {
			if (metadata.size() < 2) {
				throw new InvalidDataException("Metadata should be have atleast 2 element");
			} else {
				message.setMessageId(metadata.remove(0).getRaw());
				message.setResponseEndpoint(metadata.remove(0).getString());
			}
		}
	}

	@Override
	public void onHandleError(Message message, Throwable exception) {
		this.onHandleComplete(message, PuValue.fromObject("Error while handling message: "
				+ (exception.getMessage() == null ? "unknown error" : exception.getMessage())));
	}

	@Override
	protected void _start() {
	}

	@Override
	protected void _stop() {
	}
}

class ZeroMQResponseWriter implements Loggable, ZeroMQOutputMetadataProcessor {

	private final ZMQSocket socket;
	private Disruptor<ZeroMQResponse> disruptor;

	private ExceptionHandler<ZeroMQResponse> exceptionHandler = new ExceptionHandler<ZeroMQResponse>() {

		@Override
		public void handleEventException(Throwable ex, long sequence, ZeroMQResponse event) {
			onProcessResponseError(event, ex);
		}

		@Override
		public void handleOnStartException(Throwable ex) {
			getLogger().error("Error while shutdown response disruptor: ", ex);
		}

		@Override
		public void handleOnShutdownException(Throwable ex) {
			getLogger().error("Error while start response disruptor: ", ex);
		}
	};

	private static class Marshaller implements WorkHandler<ZeroMQResponse> {

		@Override
		public void onEvent(ZeroMQResponse event) throws Exception {
			PuArray response = new PuArrayList();
			response.addFrom(event.getMetadata());
			response.addFrom(event.getOutput());

			event.setRawOutput(response.toBytes());
		}
	}

	@SuppressWarnings("unchecked")
	ZeroMQResponseWriter(ZMQSocket socket, WorkerPoolConfig workerPoolConfig) {
		this.socket = socket;
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat("mashaller-" + workerPoolConfig.getThreadNamePattern()).build();

		this.disruptor = new Disruptor<ZeroMQResponse>(ZeroMQResponse.EVENT_FACTORY,
				workerPoolConfig.getRingBufferSize(), threadFactory);

		Marshaller[] marshallers = new Marshaller[workerPoolConfig.getMarshallerSize()];
		for (int i = 0; i < marshallers.length; i++) {
			marshallers[i] = new Marshaller();
		}

		this.disruptor.handleEventsWithWorkerPool(marshallers).then(new EventHandler<ZeroMQResponse>() {

			@Override
			public void onEvent(ZeroMQResponse event, long sequence, boolean endOfBatch) throws Exception {
				socket.send(event.getRawOutput(), ZMQ.DONTWAIT);
			}
		});
		this.disruptor.setDefaultExceptionHandler(exceptionHandler);
	}

	@Override
	public PuArray createOutputMetadata(ZeroMQResponse message) {
		PuArray metadata = new PuArrayList();
		metadata.addFrom(message.getMessageId());
		return metadata;
	}

	void start() {
		this.disruptor.start();
	}

	void stop() {
		this.socket.close();
		this.disruptor.halt();
	}

	private void onProcessResponseError(ZeroMQResponse message, Throwable ex) {
		getLogger().error("Error while writing response", ex);
	}

	void processResponse(ZeroMQMessage message, PuElement result) {
		this.disruptor.publishEvent(new EventTranslator<ZeroMQResponse>() {

			@Override
			public void translateTo(ZeroMQResponse event, long sequence) {
				event.fillData((ZeroMQMessage) message);
				event.setOutput(result);
				event.setMetadataProcessor(ZeroMQResponseWriter.this);
			}
		});
	}
}
