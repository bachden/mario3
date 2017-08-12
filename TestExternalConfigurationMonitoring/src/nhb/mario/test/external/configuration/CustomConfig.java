package nhb.mario.test.external.configuration;

import java.util.Map;

import lombok.Data;

@Data
public class CustomConfig {

	private String name;
	private Map<String, Object> map;
}
