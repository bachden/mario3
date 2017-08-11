package com.mario.external.configuration;

import java.io.File;

import com.nhb.eventdriven.EventDispatcher;

public interface ExternalConfiguration extends EventDispatcher {

	void update(File file);

	<T> T get();
}
