package com.mario.external.configuration.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.mario.external.configuration.ExternalConfigurationParser;

public class PropertiesFileParser implements ExternalConfigurationParser {

	@Override
	public Object parse(InputStream inputStream) {
		try {
			Properties props = new Properties();
			props.load(inputStream);
			return props;
		} catch (IOException e) {
			throw new RuntimeException("Error while parsing input stream", e);
		}
	}

}
