package nhb.mario3.config;

import nhb.common.data.PuObjectRO;

public class ZkClientConfig extends MarioBaseConfig {

	private String servers = "";
	private int sessionTimeout = 30000;
	private int connectionTimeout = Integer.MAX_VALUE;
	private String serializerClass = "org.I0Itec.zkclient.serialize.SerializableSerializer";
	private long operationRetryTimeout = -1L;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("servers")) {
			this.setServers(data.getString("servers"));
		}

		if (data.variableExists("sessionTimeout")) {
			this.setSessionTimeout(data.getInteger("sessionTimeout"));
		}

		if (data.variableExists("connectionTimeout")) {
			this.setConnectionTimeout(data.getInteger("connectionTimeout"));
		}

		if (data.variableExists("serializerClass")) {
			this.setSerializerClass(data.getString("serializerClass"));
		}

		if (data.variableExists("operationRetryTimeout")) {
			this.setOperationRetryTimeout(data.getLong("operationRetryTimeout"));
		}
	}

	public String getServers() {
		return servers;
	}

	public void setServers(String servers) {
		this.servers = servers;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getSerializerClass() {
		return serializerClass;
	}

	public void setSerializerClass(String serializerClass) {
		this.serializerClass = serializerClass;
	}

	public long getOperationRetryTimeout() {
		return operationRetryTimeout;
	}

	public void setOperationRetryTimeout(long operationRetryTimeout) {
		this.operationRetryTimeout = operationRetryTimeout;
	}

}
