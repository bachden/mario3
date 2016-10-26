package com.mario.entity.impl;

import java.util.ArrayList;
import java.util.List;

import com.mario.entity.MessageHandler;
import com.mario.entity.message.Message;
import com.mario.gateway.Gateway;
import com.nhb.common.data.PuElement;

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
