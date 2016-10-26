package com.mario.config;

import java.util.ArrayList;
import java.util.List;

public class MessageHandlerConfig extends LifeCycleConfig {

	private final List<String> bindingGateways = new ArrayList<String>();

	public List<String> getBindingGateways() {
		return bindingGateways;
	}
}
