package com.mario.test;

import com.hazelcast.config.Config;

import nhb.mario3.entity.HazelcastConfigPreparer;
import nhb.mario3.entity.impl.BaseLifeCycle;

public class TestHazelcastInitializer2 extends BaseLifeCycle implements HazelcastConfigPreparer {

	@Override
	public void prepareHazelcastConfig(String hazelcastName, Config config) {
		getLogger().debug("init hazelcast 2: " + config.hashCode() + ", hazelcast name: " + hazelcastName);
	}

}
