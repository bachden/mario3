package com.mario.entity.impl;

import com.mario.api.MarioApi;
import com.mario.entity.LifeCycle;
import com.mario.entity.Pluggable;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObjectRO;

public abstract class BaseLifeCycle extends BaseLoggable implements LifeCycle, Pluggable {

	private MarioApi api;
	private String name;
	private String extensionName;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getExtensionName() {
		return extensionName;
	}

	@Override
	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

	@Override
	public MarioApi getApi() {
		return this.api;
	}

	@Override
	public void setApi(MarioApi api) {
		this.api = api;
	}

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
