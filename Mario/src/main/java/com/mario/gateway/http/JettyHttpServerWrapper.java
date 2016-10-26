package com.mario.gateway.http;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.mario.config.serverwrapper.HttpServerWrapperConfig;
import com.mario.gateway.serverwrapper.BaseServerWrapper;

public class JettyHttpServerWrapper extends BaseServerWrapper {

	private ServletContextHandler handler;
	private Server server;

	@Override
	public void init() {
		if (!(getConfig() instanceof HttpServerWrapperConfig)) {
			throw new RuntimeException(
					"Illegal config, expected for " + HttpServerWrapperConfig.class.getName() + " instance");
		}

		HttpServerWrapperConfig serverConfig = (HttpServerWrapperConfig) this.getConfig();

		int capacity = serverConfig.getTaskQueueInitSize();
		capacity = capacity <= 0 ? Math.max(serverConfig.getMinAcceptorThreadPoolSize(), 8)
				: serverConfig.getTaskQueueInitSize();

		int growBy = serverConfig.getTaskQueueGrowBy();

		int maxCapacity = serverConfig.getTaskQueueMaxSize();
		maxCapacity = maxCapacity <= 0 ? Integer.MAX_VALUE : Math.max(maxCapacity, 8);

		if (maxCapacity < capacity) {
			maxCapacity = capacity;
		}

		QueuedThreadPool threadPool = new QueuedThreadPool(serverConfig.getMaxAcceptorThreadPoolSize(),
				serverConfig.getMinAcceptorThreadPoolSize(), 60000,
				new BlockingArrayQueue<>(capacity, growBy, maxCapacity));

		Server server = new Server(threadPool);
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(serverConfig.getPort());
		server.setConnectors(new Connector[] { connector });

		server.setHandler(this.getHandler());

		this.server = server;
	}

	private ServletContextHandler getHandler() {
		if (this.handler == null && this.getConfig() != null && this.getConfig() instanceof HttpServerWrapperConfig) {
			HttpServerWrapperConfig config = (HttpServerWrapperConfig) this.getConfig();
			ServletContextHandler handler = new ServletContextHandler(config.getOptions());
			if (config.getOptions() > 0 && config.getSessionTimeout() > 0) {
				handler.getSessionHandler().setMaxInactiveInterval(config.getSessionTimeout());
			}
			this.handler = handler;
		}
		return this.handler;
	}

	public boolean isRunning() {
		return this.server != null && this.server.isRunning();
	}

	@Override
	public synchronized void start() {
		if (this.isRunning()) {
			throw new IllegalStateException("Server is already running");
		}
		try {
			this.server.start();
			getLogger().info(this.getConfig().getName() + " - http server wrapper started at "
					+ ((HttpServerWrapperConfig) this.getConfig()).getPort());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void stop() {
		if (this.isRunning()) {
			try {
				this.server.stop();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new IllegalStateException("Server has been stopped");
		}
	}

	public void addServlet(ServletHolder holder, String path) {
		this.getHandler().addServlet(holder, path);
	}

	public SessionHandler getSessionHandler() {
		return this.handler.getSessionHandler();
	}
}
