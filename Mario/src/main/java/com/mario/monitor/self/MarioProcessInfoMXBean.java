package com.mario.monitor.self;

import java.util.List;

public interface MarioProcessInfoMXBean {

	List<ThreadDetails> getThreadDetails();

	int getThreadCount();

	long getUptime();
}
