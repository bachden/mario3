package com.mario.external.configuration;

import java.io.File;

import com.nhb.common.async.Callback;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventHandler;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

public class ExternalConfigurationImpl extends BaseEventDispatcher implements ExternalConfiguration {

	private Object value;
	private final ExternalConfigurationParser parser;

	ExternalConfigurationImpl(ExternalConfigurationParser parser) {
		this.parser = parser;
	}

	@Override
	public void dispatchEvent(Event event) {
		throw new RuntimeException("dispatchEvent method cannot be called directly by ExternalConfiguration");
	}

	@Override
	public void addEventListener(String eventType, EventHandler listener) {
		throw new RuntimeException(
				"addEventListener method cannot be called directly by ExternalConfiguration, use addUpdateListener instead");
	}

	public <T> void addUpdateListener(Callback<T> callback) {
		super.addEventListener(ExternalConfigurationEvent.EXTERNAL_CONFIGURATION_UPDATED, new EventHandler() {

			@Override
			public void onEvent(Event event) throws Exception {
				if (event instanceof ExternalConfigurationEvent) {
					try {
						callback.apply(((ExternalConfigurationEvent) event).getValue());
					} catch (Exception e) {
						getLogger().error("Error while dispatching configuration update event", e);
					}
				}
			}
		});
	}

	private void dispatchUpdate() {
		Event event = new ExternalConfigurationEvent(this.value);
		super.dispatchEvent(event);
	}

	@Override
	public void update(File configContent) {
		try {
			Object newValue = this.parser.parse(configContent);
			if (this.value != newValue) {
				if (this.value == null || !this.value.equals(newValue)) {
					this.value = newValue;
					this.dispatchUpdate();
				}
			}
		} catch (Exception ex) {
			getLogger().error("An error occurs while parsing external config", ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get() {
		return (T) this.value;
	}

}
