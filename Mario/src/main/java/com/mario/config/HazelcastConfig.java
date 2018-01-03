package com.mario.config;

import java.util.ArrayList;
import java.util.List;

import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;

public class HazelcastConfig extends MarioBaseConfig {

	private boolean isMember;
	private String configFilePath;
	private String initializerClass;

	private boolean lazyInit = false;
	private boolean autoInitOnExtensionReady = true;
	private final List<String> initializers = new ArrayList<>();

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("isMember")) {
			this.setMember(data.getBoolean("isMember"));
		} else if (data.variableExists("member")) {
			this.setMember(data.getBoolean("member"));
		}

		if (data.variableExists("configFilePath")) {
			this.setConfigFilePath(data.getString("configFilePath"));
		} else if (data.variableExists("configFile")) {
			this.setConfigFilePath(data.getString("configFile"));
		} else if (data.variableExists("config")) {
			this.setConfigFilePath(data.getString("config"));
		}

		if (data.variableExists("lazyinit")) {
			this.setLazyInit(data.getBoolean("lazyinit"));
		}

		if (data.variableExists("autoInit")) {
			this.setLazyInit(data.getBoolean("autoInit"));
		} else if (data.variableExists("autoInitOnExtensionReady")) {
			this.setLazyInit(data.getBoolean("autoInitOnExtensionReady"));
		}

		if (data.variableExists("initializers")) {
			for (PuValue entry : data.getPuArray("initializers")) {
				this.initializers.add(entry.getString());
			}
		}
	}

	public String getConfigFilePath() {
		return configFilePath;
	}

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public boolean isMember() {
		return isMember;
	}

	public void setMember(boolean isMember) {
		this.isMember = isMember;
	}

	public String getInitializerClass() {
		return initializerClass;
	}

	public void setInitializerClass(String initializerClass) {
		this.initializerClass = initializerClass;
	}

	public boolean isLazyInit() {
		return lazyInit;
	}

	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	public List<String> getInitializers() {
		return initializers;
	}

	public boolean isAutoInitOnExtensionReady() {
		return autoInitOnExtensionReady;
	}

	public void setAutoInitOnExtensionReady(boolean autoInitOnExtensionReady) {
		this.autoInitOnExtensionReady = autoInitOnExtensionReady;
	}
}
