package com.mario.config;

import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExternalConfigurationConfig extends MarioBaseConfig {

	private boolean monitored = false;
	private String filePath;
	private String parser;
	private String sensitivity;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		this.setMonitored(data.getBoolean("monitored", false));
		this.setParser(data.getString("parser", null));
		this.setFilePath(data.getString("filePath", null));
		this.setSensitivity(data.getString("sensitivity", null));
	}
}
