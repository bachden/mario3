package com.mario.external.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.nhb.common.data.PuObject;

public interface ExternalConfigurationParser {

	Object parse(InputStream inputStream);

	default void init(PuObject initParams) {
		// do nothing
	}

	default Object parse(File file) {
		if (file == null || !file.isFile() || !file.exists()) {
			return null;
		}
		try (InputStream inputStream = new FileInputStream(file)) {
			return this.parse(inputStream);
		} catch (Exception e) {
			throw new RuntimeException("Cannot read external configuration file", e);
		}
	}

}
