package com.mario.entity.impl;

import com.mario.api.MarioApi;
import com.mario.entity.LifeCycle;
import com.mario.entity.Pluggable;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class BaseLifeCycle extends BaseLoggable implements LifeCycle, Pluggable {

	private String name;
	private String extensionName;

	private MarioApi api;

	@Override
	public void init(PuObjectRO initParams) {
		// do nothing
	}

	@Override
	public void destroy() throws Exception {
		// do nothing
	}

	@Override
	public void onExtensionReady() {
		// do nothing
	}

	@Override
	public void onServerReady() {
		// do nothing
	}

}
