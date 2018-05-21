package com.mario.config.serverwrapper;

import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

public class ZeroMQServerWrapperConfig extends ServerWrapperConfig {

	@Setter
	@Getter
	private String zeroMQRegistryName;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		this.setZeroMQRegistryName(data.getString("zeroMQRegistryName", data.getString("zeromqregistryname", null)));
		if (this.getZeroMQRegistryName() == null) {
			throw new NullPointerException("zeroMQ registry name cannot be null");
		}
	}

}
