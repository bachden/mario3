package com.mario.gateway.websocket;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.mario.entity.message.transcoder.MessageEncoder;
import com.mario.gateway.socket.SocketReceiver;
import com.mario.gateway.socket.SocketSessionManager;
import com.mario.gateway.socket.tcp.NettyTCPSocketSession;
import com.nhb.common.data.PuElement;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCounted;

@SuppressWarnings("deprecation")
public class NettyWebSocketSession extends NettyTCPSocketSession {

	private static final String X_REAL_PORT = "X-Real-Port";
	private static final String X_REAL_IP = "X-Real-IP";
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";

	private WebSocketServerHandshaker handshaker;
	private String path = "";
	private String proxy = null;

	private boolean ssl = false;
	private InetSocketAddress proxiedRemoteAddress;
	private boolean autoActive = true;

	public NettyWebSocketSession(String gatewayName, boolean ssl, String path, String proxy,
			InetSocketAddress inetSocketAddress, SocketSessionManager sessionManager, SocketReceiver receiver,
			MessageEncoder serializer, boolean autoActive) {
		super(inetSocketAddress, sessionManager, receiver, serializer);
		this.ssl = ssl;
		this.proxy = proxy;
		this.path = path == null ? "" : path;
		if (!this.path.startsWith("/")) {
			path = "/" + path;
		}
		this.autoActive = autoActive;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws IOException {
		// do nothing
		if (this.autoActive) {
			super.channelActive(ctx);
		} else {
			getLogger()
					.info("Websocket mark as not-autoActiveChannel... waiting for the first message to active it...");
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
		try {
			if (msg instanceof FullHttpRequest) {
				getLogger().debug("Http query...");
				FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
				handleHttpRequest(ctx, fullHttpRequest);
			} else if (msg instanceof WebSocketFrame) {
				if (!this.autoActive && this.getId() == null) {
					getLogger().debug("Channel read first times: {}", ctx);
					this.setChannelHandlerContext(ctx);
					this.setId(this.getSessionManager().register(this));
					this.getReceiver().sessionOpened(this.getId());
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				handleWebSocketFrame(ctx, (WebSocketFrame) msg);
			} else {
				getLogger()
						.error("NettyWebsocketSession expected for FullHttpRequest or WebSocketFrame instance, but got "
								+ (msg == null ? "NULL" : msg.getClass()));
				throw new RuntimeException("Unable to handle message type : " + msg.getClass());
			}
		} finally {
			if (msg instanceof ReferenceCounted) {
				try {
					((ReferenceCounted) msg).release();
				} catch (Exception e) {
					getLogger().error("Error while retaining websocket message", e);
				}
			}
		}
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		// Handle a bad request.
		if (!req.getDecoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		// Allow only GET methods.
		if (req.getMethod() != GET) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		// Send the demo page and favicon.ico
		if ("/".equals(req.getUri())) {
			ByteBuf content = WebSocketServerIndexPage.getContent(getWebSocketLocation(req));
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

			res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
			HttpHeaders.setContentLength(res, content.readableBytes());

			sendHttpResponse(ctx, req, res);
			return;
		}

		if ("/favicon.ico".equals(req.getUri())) {
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
			sendHttpResponse(ctx, req, res);
			return;
		}

		if (this.proxy != null) {
			String realIp = req.headers().get(X_FORWARDED_FOR);
			if (realIp != null && !realIp.trim().isEmpty()) {
				realIp = realIp.split(",")[0];
			} else {
				realIp = req.headers().get(X_REAL_IP);
			}

			int realPort = 0;

			String realPortStr = req.headers().get(X_REAL_PORT);

			if (realPortStr != null) {
				try {
					realPort = Integer.valueOf(realPortStr);
				} catch (Exception e) {
					getLogger().warn("Error while pasing X-Real-Port header", e);
				}
			}

			getLogger().debug("Real remote address: {}:{}", realIp, realPort);
			try {
				this.proxiedRemoteAddress = new InetSocketAddress(InetAddress.getByName(realIp), realPort);
			} catch (UnknownHostException e) {
				getLogger().error("Error while create real ip", e);
			}
		}

		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req),
				null, true);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(
					String.format("%s frame types not supported", frame.getClass().getName()));
		}

		String request = ((TextWebSocketFrame) frame).text();
		this.getReceiver().receive(getId(), request);
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpHeaders.setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private String getWebSocketLocation(FullHttpRequest req) {
		String location = this.proxy != null ? this.proxy : (req.headers().get(HOST) + path);
		if (ssl) {
			return "wss://" + location;
		} else {
			return "ws://" + location;
		}
	}

	@Override
	public void send(Object obj) {
		if (obj == null) {
			return;
		}
		String msg = null;
		if (obj instanceof String) {
			msg = (String) obj;
		} else if (obj instanceof PuElement) {
			msg = ((PuElement) obj).toJSON();
		} else if (this.getSerializer() != null) {
			try {
				Object serializedData = this.getSerializer().encode(obj);
				if (serializedData instanceof String) {
					msg = (String) serializedData;
				} else {
					throw new IllegalArgumentException("Unable to send message of type " + obj.getClass()
							+ ", after serialize: " + serializedData);
				}
			} catch (Exception e) {
				throw new RuntimeException("Error while serializing message", e);
			}
		} else if (obj instanceof byte[]) {
			msg = new String((byte[]) obj);
		} else if (obj instanceof PuElement) {
			msg = ((PuElement) obj).toJSON();
		}
		if (msg != null) {
			try {
				this.send(msg);
			} catch (IOException e) {
				throw new RuntimeException("Error while sending message", e);
			}
		}
	}

	public void send(String msg) throws IOException {
		this.getChannelHandlerContext().writeAndFlush(new TextWebSocketFrame(msg));
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		if (this.proxy != null) {
			return this.proxiedRemoteAddress;
		}
		return super.getRemoteAddress();
	}
}
