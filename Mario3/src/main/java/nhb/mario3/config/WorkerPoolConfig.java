package nhb.mario3.config;

import nhb.common.data.PuObjectRO;

public class WorkerPoolConfig {

	private String threadNamePattern = "Worker #%d";
	private int ringBufferSize = 1024;
	private int poolSize = 1;

	public void readPuObject(PuObjectRO data) {
		if (data.variableExists("threadNamePattern")) {
			this.setThreadNamePattern(data.getString("threadNamePattern"));
		}

		if (data.variableExists("ringBufferSize")) {
			this.setRingBufferSize(data.getInteger("ringBufferSize"));
		}

		if (data.variableExists("poolSize")) {
			this.setPoolSize(data.getInteger("poolSize"));
		}
	}

	public String getThreadNamePattern() {
		return threadNamePattern;
	}

	public void setThreadNamePattern(String threadNamePattern) {
		this.threadNamePattern = threadNamePattern;
	}

	public int getRingBufferSize() {
		return ringBufferSize;
	}

	public void setRingBufferSize(int ringBufferSize) {
		this.ringBufferSize = ringBufferSize;
	}

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	@Override
	public String toString() {
		return String.format("Thread name pattern: %s, ringbuffer size: %d, pool size: %d", this.getThreadNamePattern(),
				this.getRingBufferSize(), this.getPoolSize());
	}
}
