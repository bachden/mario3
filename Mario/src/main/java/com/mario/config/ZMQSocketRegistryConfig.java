package com.mario.config;

import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ZMQSocketRegistryConfig extends MarioBaseConfig {

	private int numIOThreads = 1;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("name")) {
			this.setName(data.getString("name"));
		}
		if (data.variableExists("numIOThreads")) {
			this.setNumIOThreads(data.getInteger("numIOThreads"));
		}
	}

}
