package com.mario.config;

import java.util.Properties;

import com.mario.exceptions.OperationNotSupported;
import com.nhb.common.data.PuObjectRO;

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
