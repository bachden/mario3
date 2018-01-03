package com.mario.config;

import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExternalConfigurationConfig extends MarioBaseConfig {

	@Setter
	@Getter
	public static class ExternalConfigurationParserConfig {
		private String handler;
		private PuObject initParams;

		private ExternalConfigurationParserConfig readPuObject(PuObjectRO data) {
			if (data != null) {
				this.setHandler(data.getString("handler", null));
				this.setInitParams(data.getPuObject("initParams"));
			}
			return this;
		}
	}

	private boolean monitored = false;
	private String filePath;
	private ExternalConfigurationParserConfig parserConfig;
	private String sensitivity;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		this.setMonitored(data.getBoolean("monitored", false));
		this.setParserConfig(new ExternalConfigurationParserConfig().readPuObject(data.getPuObject("parser", null)));
		this.setFilePath(data.getString("filePath", null));
		this.setSensitivity(data.getString("sensitivity", null));
	}
}
