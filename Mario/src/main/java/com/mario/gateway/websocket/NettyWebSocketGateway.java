package com.mario.gateway.websocket;

import com.mario.entity.message.transcoder.websocket.WebSocketDefaultDeserializer;
import com.mario.entity.message.transcoder.websocket.WebSocketDefaultSerializer;
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

public class NettyWebSocketGateway extends NettyTCPSocketGateway {

	@Override
	protected void _init() {
		super._init();
		if (this.getSerializer() == null) {
			this.setSerializer(new WebSocketDefaultSerializer());
		}
		if (this.getDeserializer() == null) {
			this.setDeserializer(new WebSocketDefaultDeserializer());
		}
	}

	@Override
	protected void __start() {

		getLogger().debug("Starting WebSocket Gateway at: " + getConfig().getHost() + ":" + getConfig().getPort());

		// final ThreadFactory bossThreadFactory = new
		// DefaultThreadFactory(getName() + "-gateway-accept");
		// final ThreadFactory workerThreadFactory = new
		// DefaultThreadFactory(getName() + "-gateway-connect");

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
				pipeline.addLast(new HttpServerCodec());
				pipeline.addLast(new HttpObjectAggregator(65536));
				pipeline.addLast(new NettyWebSocketSession(getName(), getConfig().isSsl(), ch.remoteAddress(),
						NettyWebSocketGateway.this.getSessionManager(), NettyWebSocketGateway.this,
						NettyWebSocketGateway.this.getSerializer()));
			}
		});

		// Bind and start to accept incoming connections.
		channelFuture = getConfig().getHost() != null ? bootstrap.bind(getConfig().getHost(), getConfig().getPort())
				: bootstrap.bind(getConfig().getPort());

		getLogger().info(getName() + " gateway started...");
	}

}
