package com.mario.gateway.socket.udt;

import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLEngine;

import com.mario.gateway.socket.tcp.NettyTCPSocketGateway;
import com.mario.gateway.socket.tcp.NettyTCPSocketSession;
import com.nhb.messaging.socket.netty.codec.MsgpackDecoder;
import com.nhb.messaging.socket.netty.codec.MsgpackEncoder;

import io.netty.bootstrap.ChannelFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

@SuppressWarnings("deprecation")
public class NettyUDTSocketGateway extends NettyTCPSocketGateway {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void __start() {

		getLogger().debug("Starting UDT Socket Gateway at: " + getConfig().getHost() + ":" + getConfig().getPort());

		final ThreadFactory acceptFactory = new DefaultThreadFactory(getName() + "-gateway-accept");
		final ThreadFactory connectFactory = new DefaultThreadFactory(getName() + "-gateway-connect");

		bossGroup = new NioEventLoopGroup(this.getConfig().getBootEventLoopGroupThreads(), acceptFactory,
				NioUdtProvider.BYTE_PROVIDER);
		workerGroup = new NioEventLoopGroup(this.getConfig().getWorkerEventLoopGroupThreads(), connectFactory,
				NioUdtProvider.BYTE_PROVIDER);

		bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup).channelFactory((ChannelFactory) NioUdtProvider.BYTE_ACCEPTOR)
				.handler(new LoggingHandler(LogLevel.INFO)).option(ChannelOption.SO_BACKLOG, 10)
				.option(ChannelOption.SO_TIMEOUT, 10);

		bootstrap.childHandler(new ChannelInitializer<UdtChannel>() {
			@Override
			public void initChannel(UdtChannel ch) throws Exception {
				if (getConfig().isSsl()) {
					SSLEngine sslEngine = getSSLEngine();
					if (sslEngine != null) {
						ch.pipeline().addLast("ssl", new SslHandler(sslEngine));
					}
				}
				getLogger().debug("use length prepender: " + getConfig().isUseLengthPrepender());
				if (getConfig().isUseLengthPrepender()) {
					ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(4));
					ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
				}
				ch.pipeline().addLast("msgpackEncoder", MsgpackEncoder.newInstance());
				ch.pipeline().addLast("msgpackDecoder", MsgpackDecoder.newInstance());
				ch.pipeline()
						.addLast(new NettyTCPSocketSession(ch.remoteAddress(),
								NettyUDTSocketGateway.this.getSessionManager(), NettyUDTSocketGateway.this,
								NettyUDTSocketGateway.this.getSerializer()));
			}
		});

		// Bind and start to accept incoming connections.
		channelFuture = getConfig().getHost() != null ? bootstrap.bind(getConfig().getHost(), getConfig().getPort())
				: bootstrap.bind(getConfig().getPort());

		getLogger().info(getName() + " gateway started...");
	}

}
