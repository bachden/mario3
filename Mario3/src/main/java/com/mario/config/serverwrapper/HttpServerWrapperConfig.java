package com.mario.config.serverwrapper;

import com.mario.gateway.http.JettyHttpServerOptions;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class HttpServerWrapperConfig extends ServerWrapperConfig {

	private String host;
	private int port;
	private int options = 0;
	private int sessionTimeout = 1200; // s

	private int minAcceptorThreadPoolSize = 2;
	private int maxAcceptorThreadPoolSize = 16;

	private int taskQueueInitSize = 8;
	private int taskQueueGrowBy = -1; // use default in Blocking
	private int taskQueueMaxSize = Integer.MAX_VALUE;

	{
		this.setType(ServerWrapperType.HTTP);
	}

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("host")) {
			this.setHost(data.getString("host"));
		}
		if (data.variableExists("port")) {
			this.setPort(data.getInteger("port"));
		}
		if (data.variableExists("options")) {
			this.setOptions(JettyHttpServerOptions.fromName(data.getString("options")).getCode());
		}
		if (data.variableExists("sessionTimeout")) {
			this.setSessionTimeout(data.getInteger("sessionTimeout"));
		}
		if (data.variableExists("threadPool")) {
			PuObject threadPoolConfig = data.getPuObject("threadPool");
			if (threadPoolConfig.variableExists("minSize")) {
				this.setMinAcceptorThreadPoolSize(threadPoolConfig.getInteger("minSize"));
			}
			if (threadPoolConfig.variableExists("maxSize")) {
				this.setMaxAcceptorThreadPoolSize(threadPoolConfig.getInteger("maxSize"));
			}
			if (threadPoolConfig.variableExists("taskQueue")) {
				PuObject taskQueueConfig = threadPoolConfig.getPuObject("taskQueue");
				if (taskQueueConfig.variableExists("initSize")) {
					this.setTaskQueueInitSize(taskQueueConfig.getInteger("initSize"));
				}
				if (taskQueueConfig.variableExists("growBy")) {
					this.setTaskQueueGrowBy(taskQueueConfig.getInteger("growBy"));
				}
				if (taskQueueConfig.variableExists("maxSize")) {
					this.setTaskQueueMaxSize(taskQueueConfig.getInteger("maxSize"));
				}
			}
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getOptions() {
		return options;
	}

	public void setOptions(int options) {
		this.options = options;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public int getMinAcceptorThreadPoolSize() {
		return minAcceptorThreadPoolSize;
	}

	public void setMinAcceptorThreadPoolSize(int minAcceptorThreadPoolSize) {
		this.minAcceptorThreadPoolSize = minAcceptorThreadPoolSize;
	}

	public int getMaxAcceptorThreadPoolSize() {
		return maxAcceptorThreadPoolSize;
	}

	public void setMaxAcceptorThreadPoolSize(int maxAcceptorThreadPoolSize) {
		this.maxAcceptorThreadPoolSize = maxAcceptorThreadPoolSize;
	}

	public int getTaskQueueGrowBy() {
		return taskQueueGrowBy;
	}

	public void setTaskQueueGrowBy(int taskQueueGrowBy) {
		this.taskQueueGrowBy = taskQueueGrowBy;
	}

	public int getTaskQueueMaxSize() {
		return taskQueueMaxSize;
	}

	public void setTaskQueueMaxSize(int taskQueueMaxSize) {
		this.taskQueueMaxSize = taskQueueMaxSize;
	}

	public int getTaskQueueInitSize() {
		return taskQueueInitSize;
	}

	public void setTaskQueueInitSize(int taskQueueInitSize) {
		this.taskQueueInitSize = taskQueueInitSize;
	}

}
