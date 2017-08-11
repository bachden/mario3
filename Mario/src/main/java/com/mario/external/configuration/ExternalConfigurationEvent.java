package com.mario.external.configuration;

import com.nhb.eventdriven.impl.AbstractEvent;

public class ExternalConfigurationEvent extends AbstractEvent {

	public static final String EXTERNAL_CONFIGURATION_UPDATED = "externalConfigurationUpdated";

	private final Object value;

	public ExternalConfigurationEvent(Object value) {
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue() {
		return (T) this.value;
	}
}
