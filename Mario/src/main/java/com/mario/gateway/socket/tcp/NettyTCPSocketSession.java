package com.mario.gateway.socket.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mario.entity.message.transcoder.MessageEncoder;
import com.mario.gateway.socket.SocketReceiver;
import com.mario.gateway.socket.SocketSession;
import com.mario.gateway.socket.SocketSessionManager;
import com.mario.gateway.socket.event.SocketSessionEvent;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventDispatcher;
import com.nhb.eventdriven.EventHandler;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultChannelPromise;

public class NettyTCPSocketSession extends ChannelInboundHandlerAdapter implements Loggable, SocketSession {

	private String id;
	private ChannelHandlerContext channelHandlerContext;
	private SocketSessionManager sessionManager;

	private InetSocketAddress remoteSocketAddress;

	private Logger logger;
	private SocketReceiver receiver;
	private MessageEncoder serializer;

	public NettyTCPSocketSession(InetSocketAddress inetSocketAddress, SocketSessionManager sessionManager,
			SocketReceiver receiver, MessageEncoder messageSerializer) {
		this.remoteSocketAddress = inetSocketAddress;
		this.receiver = receiver;
		this.sessionManager = sessionManager;
		this.serializer = messageSerializer;
	}

	@Override
	public Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(this.getClass());
		}
		return logger;
	}

	@Override
	public Logger getLogger(String name) {
		return LoggerFactory.getLogger(name);
	}

	public SocketReceiver getReceiver() {
		return receiver;
	}

	@Override
	public void send(Object obj) {
		if (!this.isActive()) {
			throw new RuntimeException("Channel context hasn't been activated");
		}

		if (this.getSerializer() != null) {
			try {
				obj = this.serializer.encode(obj);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Unable to send message which isn't type of byte array while message serializer doesn't return byte[]");
			}
		}

		if (obj instanceof PuElement || obj instanceof byte[] || obj instanceof String) {
			this.getChannelHandlerContext().writeAndFlush(obj);
		}
	}

	@Override
	public void sendPromise(Object obj) throws InterruptedException {
		if (!this.isActive()) {
			throw new RuntimeException("Channel context hasn't been activated");
		}

		if (obj instanceof PuElement) {
			this.getChannelHandlerContext()
					.writeAndFlush(obj, new DefaultChannelPromise(this.getChannelHandlerContext().channel())).sync();
		} else {
			byte[] bytes = null;
			if (obj instanceof byte[]) {
				bytes = (byte[]) obj;
			} else if (this.serializer != null) {
				try {
					Object serialziedObj = this.serializer.encode(obj);
					if (serialziedObj instanceof byte[]) {
						bytes = (byte[]) serialziedObj;
					} else {
						throw new IllegalArgumentException(
								"Unable to send message which isn't type of byte array while message serializer doesn't return byte[]");
					}
				} catch (Exception e) {
					throw new RuntimeException("Error while serializing data", e);
				}
			}
			if (bytes != null) {
				ByteBuf response = Unpooled.buffer(bytes.length);
				response.writeBytes(bytes);
				this.getChannelHandlerContext()
						.writeAndFlush(response, new DefaultChannelPromise(this.getChannelHandlerContext().channel()))
						.sync();
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (this.stopHandling()) {
			this.getChannelHandlerContext().close();
			this.setChannelHandlerContext(null);
		}
	}

	@Override
	public void closeSync() throws IOException, InterruptedException {
		if (this.stopHandling()) {
			this.getChannelHandlerContext().close().sync();
			this.setChannelHandlerContext(null);
		}
	}

	@Override
	public boolean isActive() {
		return this.getChannelHandlerContext() != null;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws IOException {
		this.setChannelHandlerContext(ctx);
		this.setId(this.getSessionManager().register(this));
		// getLogger().debug("socket active: " + this.getId());
		this.receiver.sessionOpened(this.getId());
	}

	private final AtomicBoolean stopFlag = new AtomicBoolean(false);

	private boolean stopHandling() {
		String _id = null;
		SocketReceiver _receiver = null;

		if (this.stopFlag.compareAndSet(false, true)) {
			_id = this.getId();
			this.setId(null);

			_receiver = this.getReceiver();
			this.receiver = null;
		}

		if (_id != null) {
			this.sessionManager.deregister(_id);
			this.dispatchEvent(new SocketSessionEvent(SocketSessionEvent.SESSION_CLOSED, _id));
			_receiver.sessionClosed(_id);
			return true;
		}

		return false;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws IOException {
		this.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
		if (this.getId() == null) {
			getLogger().debug("Channel is not active or being closed");
		} else {
			this.receiver.receive(getId(), msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws InterruptedException {
		// getLogger().debug("channel read complete");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		getLogger().error("error on socket session " + this.getId(), cause);
		if (cause instanceof IOException && cause.getMessage().contains("Connection reset by peer")) {
			try {
				this.channelInactive(ctx);
			} catch (Exception e) {
				getLogger().error("Cannot call channel inactive...", e);
			}
		}
	}

	public InetSocketAddress getRemoteAddress() {
		return this.remoteSocketAddress;
	}

	protected ChannelHandlerContext getChannelHandlerContext() {
		return channelHandlerContext;
	}

	@Override
	public String getId() {
		return this.id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	protected void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
		this.channelHandlerContext = channelHandlerContext;
	}

	protected SocketSessionManager getSessionManager() {
		return sessionManager;
	}

	public MessageEncoder getSerializer() {
		return serializer;
	}

	public void setSerializer(MessageEncoder serializer) {
		this.serializer = serializer;
	}

	private EventDispatcher eventDispatcher = new BaseEventDispatcher();

	public void addEventListener(String eventType, EventHandler listener) {
		eventDispatcher.addEventListener(eventType, listener);
	}

	public void removeEventListener(String eventType, EventHandler listener) {
		eventDispatcher.removeEventListener(eventType, listener);
	}

	public void removeAllEventListener() {
		eventDispatcher.removeAllEventListener();
	}

	public void dispatchEvent(Event event) {
		if (event != null) {
			if (event.getTarget() == null) {
				event.setTarget(this);
			}
			eventDispatcher.dispatchEvent(event);
		}
	}

	public void dispatchEvent(String eventType, Object... data) {
		eventDispatcher.dispatchEvent(eventType, data);
	}
}
