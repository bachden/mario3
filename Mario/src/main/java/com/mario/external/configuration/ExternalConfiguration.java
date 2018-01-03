package com.mario.external.configuration;

import java.io.File;

import com.nhb.common.async.Callback;

public interface ExternalConfiguration {

	<T> void addUpdateListener(Callback<T> listener);

	void update(File file);

	<T> T get();
}
