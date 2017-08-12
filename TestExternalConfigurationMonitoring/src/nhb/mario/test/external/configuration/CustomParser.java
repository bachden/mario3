package nhb.mario.test.external.configuration;

import java.io.InputStream;

import com.mario.external.configuration.parser.YamlFileParser;

public class CustomParser extends YamlFileParser {

	@Override
	public Object parse(InputStream inputStream) {
		try {
			CustomConfig customConfig = (CustomConfig) super.parse(inputStream, CustomConfig.class);
			return "name: " + customConfig.getName();
		} catch (Exception e) {
			throw new RuntimeException("Error while parsing input stream", e);
		}
	}
}
