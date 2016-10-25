package nhb.mario3.config.gateway;

import nhb.common.data.PuObjectRO;
import nhb.mario3.config.MarioBaseConfig;
import nhb.mario3.config.WorkerPoolConfig;

public abstract class GatewayConfig extends MarioBaseConfig {

	private String serverWrapperName;
	private String deserializerClassName;
	private String serializerClassName;
	private GatewayType type;
	private boolean ssl = false;
	private WorkerPoolConfig workerPoolConfig;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("serverWrapperName")) {
			this.setServerWrapperName(data.getString("serverWrapperName"));
		} else if (data.variableExists("server")) {
			this.setServerWrapperName(data.getString("server"));
		}

		if (data.variableExists("deserializerClassName")) {
			this.setDeserializerClassName(data.getString("deserializerClassName"));
		} else if (data.variableExists("deserializer")) {
			this.setDeserializerClassName(data.getString("deserializer"));
		}

		if (data.variableExists("serializerClassName")) {
			this.setSerializerClassName(data.getString("serializerClassName"));
		} else if (data.variableExists("serializer")) {
			this.setSerializerClassName(data.getString("serializer"));
		}

		if (data.variableExists("ssl")) {
			this.setSsl(data.getBoolean("ssl"));
		}

		if (data.variableExists("workerPool")) {
			if (this.workerPoolConfig == null) {
				this.workerPoolConfig = new WorkerPoolConfig();
			}
			this.workerPoolConfig.readPuObject(data.getPuObject("workerPool"));
		}
	}

	public String getSerializerClassName() {
		return serializerClassName;
	}

	public void setSerializerClassName(String serializerClassName) {
		this.serializerClassName = serializerClassName;
	}

	public String getDeserializerClassName() {
		return deserializerClassName;
	}

	public void setDeserializerClassName(String deserializerClassName) {
		if (deserializerClassName == null || deserializerClassName.trim().length() == 0)
			return;
		this.deserializerClassName = deserializerClassName;
	}

	public GatewayType getType() {
		return type;
	}

	protected void setType(GatewayType type) {
		this.type = type;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public WorkerPoolConfig getWorkerPoolConfig() {
		return workerPoolConfig;
	}

	public void setWorkerPoolConfig(WorkerPoolConfig workerPoolConfig) {
		this.workerPoolConfig = workerPoolConfig;
	}

	public String getServerWrapperName() {
		return serverWrapperName;
	}

	public void setServerWrapperName(String serverWrapperName) {
		this.serverWrapperName = serverWrapperName;
	}

}
