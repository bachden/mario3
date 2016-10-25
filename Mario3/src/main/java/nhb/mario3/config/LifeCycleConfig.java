package nhb.mario3.config;

import nhb.common.data.PuObject;
import nhb.common.data.PuObjectRO;
import nhb.mario3.exceptions.OperationNotSupported;

public class LifeCycleConfig extends MarioBaseConfig {

	private PuObject initParams;
	private String handleClass;

	@Override
	protected final void _readPuObject(PuObjectRO data) {
		throw new OperationNotSupported("Reference to whole config is not allowed in lifeCycle");
	}

	public PuObject getInitParams() {
		return initParams;
	}

	public void setInitParams(PuObject initParams) {
		this.initParams = initParams;
	}

	public String getHandleClass() {
		return handleClass;
	}

	public void setHandleClass(String handleClass) {
		this.handleClass = handleClass;
	}
}
