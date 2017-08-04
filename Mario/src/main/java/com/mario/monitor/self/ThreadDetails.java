package com.mario.monitor.self;

import java.lang.Thread.State;
import java.text.DecimalFormat;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ThreadDetails implements Comparable<ThreadDetails> {

	private float cpuUsage;

	@Getter
	private long id;
	@Getter
	private String name;
	@Getter
	private String stacktrace;
	@Getter
	private State state;

	public String getCpu() {
		return new DecimalFormat("#.##%").format(this.cpuUsage);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getName()).append(" [").append(this.getState()).append("] - ").append(this.getCpu());
		return result.toString();
	}

	@Override
	public int compareTo(ThreadDetails o) {
		return this.cpuUsage < o.cpuUsage ? 1 : -1;
	}
}
