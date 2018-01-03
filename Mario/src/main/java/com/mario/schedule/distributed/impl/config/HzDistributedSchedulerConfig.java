package com.mario.schedule.distributed.impl.config;

import lombok.Data;

@Data
public class HzDistributedSchedulerConfig {
	private String name;
	private String hazelcastName;
}
