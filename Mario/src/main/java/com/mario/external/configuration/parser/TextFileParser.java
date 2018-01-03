package com.mario.external.configuration.parser;

import java.io.InputStream;

public class TextFileParser extends RawFileParser {

	@Override
	public Object parse(InputStream inputStream) {
		return new String((byte[]) super.parse(inputStream));
	}
}
