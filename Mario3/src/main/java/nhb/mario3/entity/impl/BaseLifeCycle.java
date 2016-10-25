package nhb.mario3.entity.impl;

import nhb.common.BaseLoggable;
import nhb.common.data.PuObjectRO;
import nhb.mario3.api.MarioApi;
import nhb.mario3.entity.LifeCycle;
import nhb.mario3.entity.Pluggable;

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
