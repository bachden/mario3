package nhb.mario3.entity.impl;

import java.util.ArrayList;
import java.util.List;

import nhb.common.data.PuElement;
import nhb.mario3.entity.MessageHandler;
import nhb.mario3.entity.message.Message;
import nhb.mario3.gateway.Gateway;

public abstract class BaseMessageHandler extends BaseLifeCycle implements MessageHandler {

	private List<Gateway> gateways = new ArrayList<Gateway>();

	public List<Gateway> getGateways() {
		return this.gateways;
	}

	@Override
	public void bind(Gateway gateway) {
		if (gateway != null && !this.getGateways().contains(gateway)) {
			gateway.setHandler(this);
			this.getGateways().add(gateway);
		} else {
			throw new RuntimeException("gateway to be binded cannot be null");
		}
	}

	@Override
	public void destroy() throws Exception {
		super.destroy();
		for (Gateway gateway : this.getGateways()) {
			gateway.setHandler(null);
		}
	}

	@Override
	public PuElement interop(PuElement requestParams) {
		return null;
	}

	@Override
	public PuElement handle(Message message) {
		return null;
	}
}
