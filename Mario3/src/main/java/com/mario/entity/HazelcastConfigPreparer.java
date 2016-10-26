package com.mario.entity;

import com.hazelcast.config.Config;

public interface HazelcastConfigPreparer {

	/**
	 * this method called right after all lifecycle entity got initialized, but
	 * before "onExtensionReady" called
	 * 
	 * @param config:
	 *            the hazelcast to be configured
	 */
	void prepareHazelcastConfig(String name, Config config);
}
