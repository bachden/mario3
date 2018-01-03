package com.mario.external.configuration.parser;

import java.io.InputStream;
import java.util.Arrays;

public class TextAsLinesParser extends TextFileParser {

	@Override
	public Object parse(InputStream inputStream) {
		String content = (String) super.parse(inputStream);
		return Arrays.asList(content.split("\n"));
	}
}
