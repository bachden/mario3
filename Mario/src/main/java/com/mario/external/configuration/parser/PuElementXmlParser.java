package com.mario.external.configuration.parser;

import java.io.InputStream;

import com.nhb.common.data.PuXmlHelper;

public class PuElementXmlParser extends TextFileParser {

	@Override
	public Object parse(InputStream inputStream) {
		String fileContent = (String) super.parse(inputStream);
		try {
			return PuXmlHelper.parseXml(fileContent);
		} catch (Exception e) {
			throw new RuntimeException("An error occurs while parsing xml as PuElement", e);
		}
	}
}
