package com.mario.test;

import com.hazelcast.config.Config;

import nhb.mario3.entity.HazelcastConfigPreparer;
import nhb.mario3.entity.impl.BaseMessageHandler;

public class TestHazelcastInitializer1 extends BaseMessageHandler implements HazelcastConfigPreparer {

	@Override
	public void prepareHazelcastConfig(String hazelcastName, Config config) {
		getLogger().debug("init hazelcast 1: " + config.hashCode() + ", hazelcast name: " + hazelcastName);
	}

}
