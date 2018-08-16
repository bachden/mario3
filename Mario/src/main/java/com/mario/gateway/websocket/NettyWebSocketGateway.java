package com.mario.gateway.websocket;

import javax.net.ssl.SSLEngine;

import com.mario.entity.message.transcoder.websocket.WebSocketDefaultDeserializer;
import com.mario.entity.message.transcoder.websocket.WebSocketDefaultSerializer;
import com.mario.exceptions.InvalidConfigException;
import com.mario.gateway.socket.tcp.NettyTCPSocketGateway;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;

public class NettyWebSocketGateway extends NettyTCPSocketGateway {

	private String path;
	private String proxy;

	@Override
	protected void _init() {
		super._init();
		if (this.getSerializer() == null) {
			this.setSerializer(new WebSocketDefaultSerializer());
		}
		if (this.getDeserializer() == null) {
			this.setDeserializer(new WebSocketDefaultDeserializer());
		}
		this.path = this.getConfig().getPath() == null ? "/websocket" : this.getConfig().getPath();
		if (this.path.equals("/")) {
			throw new InvalidConfigException(
					"Websocket path cannot be equal / because of test page already handled at that path, extension: "
							+ this.getExtensionName());
		}
		if (!this.path.startsWith("/")) {
			this.path = "/" + this.path;
		}

		this.proxy = this.getConfig().getProxy();
	}

	@Override
	protected void __start() {

		getLogger().debug("Starting WebSocket Gateway at: "
				+ (getConfig().getHost() == null ? "0.0.0.0" : this.getConfig().getHost()) + ":" + getConfig().getPort()
				+ this.path);

		boolean autoActiveChannel = this.getConfig().isAutoActiveChannel();

		bossGroup = new NioEventLoopGroup(this.getConfig().getBootEventLoopGroupThreads());
		workerGroup = new NioEventLoopGroup(this.getConfig().getWorkerEventLoopGroupThreads());

		bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.handler(new LoggingHandler(LogLevel.INFO));
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				if (getConfig().isSsl()) {
					SSLEngine sslEngine = getSSLEngine();
					if (sslEngine != null) {
						pipeline.addLast("ssl", new SslHandler(sslEngine));
					}
				}
				pipeline.addLast(new HttpServerCodec());
				pipeline.addLast(new HttpObjectAggregator(65536));
				pipeline.addLast(new NettyWebSocketSession(getName(), getConfig().getFrameFormat(), getConfig().isSsl(),
						NettyWebSocketGateway.this.path, NettyWebSocketGateway.this.proxy, ch.remoteAddress(),
						NettyWebSocketGateway.this.getSessionManager(), NettyWebSocketGateway.this,
						NettyWebSocketGateway.this.getSerializer(), autoActiveChannel));
			}
		});

		// Bind and start to accept incoming connections.
		channelFuture = getConfig().getHost() != null ? bootstrap.bind(getConfig().getHost(), getConfig().getPort())
				: bootstrap.bind(getConfig().getPort());

		try {
			if (channelFuture.await().isSuccess()) {
				getLogger().debug("Gateway " + this.getName() + " success binding to " + getConfig().getHost() + ":"
						+ getConfig().getPort());
			} else {
				throw new RuntimeException("Start gateway " + this.getName() + " error, unable to bind to "
						+ this.getConfig().getHost() + ":" + this.getConfig().getPort());
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		getLogger().info(getName() + " gateway started...");
	}

}
