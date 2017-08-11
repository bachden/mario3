package com.mario.external.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public interface ExternalConfigurationParser {

	Object parse(InputStream inputStream);

	default Object parse(File file) {
		try (InputStream inputStream = new FileInputStream(file)) {
			return this.parse(inputStream);
		} catch (Exception e) {
			throw new RuntimeException("Cannot read external configuration file", e);
		}
	}

}
