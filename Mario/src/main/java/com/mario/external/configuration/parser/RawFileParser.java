package com.mario.external.configuration.parser;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.mario.external.configuration.ExternalConfigurationParser;

public class RawFileParser implements ExternalConfigurationParser {

	@Override
	public Object parse(InputStream inputStream) {
		try {
			return IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("Error while parsing input stream", e);
		}
	}

}
