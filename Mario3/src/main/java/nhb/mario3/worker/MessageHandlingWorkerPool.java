package nhb.mario3.worker;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkerPool;

import nhb.common.BaseLoggable;
import nhb.mario3.config.WorkerPoolConfig;
import nhb.mario3.entity.MessageHandleCallback;
import nhb.mario3.entity.MessageHandler;
import nhb.mario3.entity.message.DecodingErrorMessage;
import nhb.mario3.entity.message.Message;
import nhb.mario3.entity.message.MessageRW;
import nhb.mario3.entity.message.transcoder.MessageDecoder;
import nhb.mario3.entity.message.transcoder.MessageDecodingException;

public class MessageHandlingWorkerPool extends BaseLoggable implements ExceptionHandler<Message> {

	public static final MessageHandlingWorkerPool DEFAULT = new MessageHandlingWorkerPool();

	private WorkerPool<Message> workerPool;
	private RingBuffer<Message> ringBuffer;
	private MessageHandlingWorker[] workers;
	private WorkerPoolConfig config;

	private MessageEventFactory eventFactory;
	private MessageHandler handler;
	private MessageDecoder messageDeserializer;
	private MessageHandleCallback messageHandleCallback;

	protected MessageHandlingWorker[] getWorkers() {
		if (this.workers == null) {
			this.initWorkers();
		}
		return this.workers;
	}

	protected void initWorkers() {
		if (this.handler == null) {
			getLogger().warn("MessageHandler hasn't been set", new Exception());
		}
		this.workers = new MessageHandlingWorker[this.getConfig() == null ? 1
				: Math.max(this.getConfig().getPoolSize(), 1)];
		for (int i = 0; i < workers.length; i++) {
			this.workers[i] = new MessageHandlingWorker();
			this.workers[i].setHandler(this.handler);
			this.workers[i].setCallback(this.messageHandleCallback);
		}
	}

	public final void start(MessageEventFactory eventFactory, MessageDecoder messageDeserializer,
			MessageHandler handler, MessageHandleCallback messageHandleCallback) {

		this.handler = handler;
		this.eventFactory = eventFactory;
		this.messageDeserializer = messageDeserializer;
		this.messageHandleCallback = messageHandleCallback;

		if (this.getConfig() == null) {
			getLogger().warn("Worker pool config is null, stop creating MessageHandlingWorkerPool");
			return;
		}

		this.ringBuffer = RingBuffer.createMultiProducer(this.eventFactory, this.getConfig().getRingBufferSize());

		this.workerPool = new WorkerPool<Message>(this.ringBuffer, this.ringBuffer.newBarrier(), this, getWorkers());
		this.ringBuffer.addGatingSequences(this.workerPool.getWorkerSequences());

		this.workerPool.start(new ThreadPoolExecutor(getConfig().getPoolSize(), getConfig().getPoolSize(), 60,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

					final AtomicInteger threadNumber = new AtomicInteger(1);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r,
								String.format(getConfig().getThreadNamePattern(), threadNumber.getAndIncrement()));
					}
				}));
	}

	public Message publish(Object obj) throws MessageDecodingException {
		final Message message;
		long sequence = this.ringBuffer.next();
		try {
			message = this.ringBuffer.get(sequence);
			try {
				this.messageDeserializer.decode(obj, (MessageRW) message);
			} catch (MessageDecodingException e) {
				if (message instanceof DecodingErrorMessage) {
					((DecodingErrorMessage) message).setDecodingFailedCause(e);
				}
			}
		} finally {
			this.ringBuffer.publish(sequence);
		}
		return message;
	}

	public WorkerPoolConfig getConfig() {
		return config;
	}

	public void setConfig(WorkerPoolConfig config) {
		this.config = config;
	}

	@Override
	public void handleEventException(Throwable exception, long sequence, Message message) {
		if (messageHandleCallback != null) {
			messageHandleCallback.onHandleError(message, exception);
		} else {
			getLogger().error("An error occurs when handling message at sequence: {}, data: {}", sequence,
					message.getData(), exception);
		}
	}

	@Override
	public void handleOnShutdownException(Throwable arg0) {
		getLogger().error("An error occurs when shutting down handle message", arg0);
	}

	@Override
	public void handleOnStartException(Throwable arg0) {
		getLogger().error("An error occurs when starting handle message", arg0);
	}

	public void shutdown() {
		if (this.workerPool != null && this.workerPool.isRunning()) {
			System.out.println("shutting down worker pool...");
			this.workerPool.drainAndHalt();
			System.out.println("Worker pool shutted down");
		}
	}

}
