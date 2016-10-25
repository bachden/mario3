package nhb.mario3.config;

import java.util.Properties;

import nhb.common.data.PuObjectRO;
import nhb.mario3.exceptions.OperationNotSupported;

public class SQLConfig extends MarioBaseConfig {

	private Properties properties = new Properties();

	public Properties getProperties() {
		return properties;
	}

	@Override
	protected void _readPuObject(PuObjectRO data) {
		throw new OperationNotSupported("Read ref doesn't support for sql config right now");
	}
}
