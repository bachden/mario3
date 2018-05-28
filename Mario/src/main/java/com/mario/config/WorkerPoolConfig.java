package com.mario.config;

import java.util.concurrent.TimeUnit;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.LiteTimeoutBlockingWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
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

	static WaitStrategy waitStrategyForName(String name, long timeout, TimeUnit timeoutUnit) {
		if (name != null) {
			switch (name.trim().toLowerCase()) {
			case "blocking":
				return new BlockingWaitStrategy();
			case "busyspin":
				return new BusySpinWaitStrategy();
			case "liteblocking":
				return new LiteBlockingWaitStrategy();
			case "litetimeoutblocking":
				return new LiteTimeoutBlockingWaitStrategy(timeout, timeoutUnit);
			case "sleeping":
				return new SleepingWaitStrategy();
			case "timeoutblocking":
				return new TimeoutBlockingWaitStrategy(timeout, timeoutUnit);
			case "yielding":
				return new YieldingWaitStrategy();
			default:
				throw new IllegalArgumentException(
						"Wait strategy only support blocking, busySpin, liteBlocking, liteTimeoutBlocking, sleeping, timeoutBlocking, yielding. Got: "
								+ name);
			}
		}
		throw new NullPointerException("WaitStrategy name cannot be null");
	}

	public void readNode(Node item) {
		if (item != null) {
			Node curr = item.getFirstChild();
			String waitStrategyName = null;
			long waitStrategyTimeoutNano = (long) 1e9;
			while (curr != null) {
				if (curr.getNodeType() == Element.ELEMENT_NODE) {
					String value = curr.getTextContent().trim();
					String nodeName = curr.getNodeName();
					switch (nodeName.trim().toLowerCase()) {
					case "poolsize":
						this.setPoolSize(Integer.valueOf(value));
						break;
					case "ringbuffersize":
						this.setRingBufferSize(Integer.valueOf(value));
						break;
					case "threadnamepattern":
						this.setThreadNamePattern(value);
						break;
					case "marshallersize":
						this.setMarshallerSize(Integer.valueOf(value));
						break;
					case "unmarshallersize":
						this.setUnmarshallerSize(Integer.valueOf(value));
						break;
					case "waitstrategy":
						waitStrategyName = value;
						break;
					case "waitstrategytimeoutnano":
						waitStrategyTimeoutNano = Long.valueOf(value);
						break;
					}
				}
				curr = curr.getNextSibling();
			}
			if (waitStrategyName != null) {
				this.setWaitStrategy(
						waitStrategyForName(waitStrategyName, waitStrategyTimeoutNano, TimeUnit.NANOSECONDS));
			}
		} else {
			throw new NullPointerException("item to read as worker pool config cannot be null");
		}
	}

	@Override
	public String toString() {
		return String.format("Thread name pattern: %s, ringbuffer size: %d, pool size: %d", this.getThreadNamePattern(),
				this.getRingBufferSize(), this.getPoolSize());
	}
}
