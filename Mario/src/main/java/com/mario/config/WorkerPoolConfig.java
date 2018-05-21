package com.mario.config;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkerPoolConfig {

	private String threadNamePattern = "Worker #%d";
	private int ringBufferSize = 1024;
	private int poolSize = 1;
	private WaitStrategy waitStrategy = new BlockingWaitStrategy();
	private int unmarshallerSize = 2;
	private int marshallerSize = 2;

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

		if (data.variableExists("unmarshallerSize")) {
			this.setUnmarshallerSize(data.getInteger("unmarshallerSize"));
		}

		if (data.variableExists("marshallerSize")) {
			this.setMarshallerSize(data.getInteger("marshallerSize"));
		}
	}

	@Override
	public String toString() {
		return String.format("Thread name pattern: %s, ringbuffer size: %d, pool size: %d", this.getThreadNamePattern(),
				this.getRingBufferSize(), this.getPoolSize());
	}
}
