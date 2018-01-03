package nhb.mario.test.external.configuration;

import java.util.List;

import lombok.Data;

@Data
public class CustomConfig {

	private String name;
	private List<Element> data;
}
