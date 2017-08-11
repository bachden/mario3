package com.mario.external.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mario.config.ExternalConfigurationConfig;
import com.mario.extension.ExtensionManager;
import com.nhb.common.async.Callback;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.common.utils.StringUtils;

public class ExternalConfigurationManager {

	private FileWatcher fileWatcher;
	private Map<String, ExternalConfiguration> instances;

	private final List<ExternalConfigurationConfig> waitingForInitConfigs = new ArrayList<>();

	public ExternalConfiguration getConfig(String name) {
		return this.instances.get(name);
	}

	public void init(ExtensionManager extensionManager) {
		Map<MonitoredFile, Set<ExternalConfiguration>> fileToInstanceName = new ConcurrentHashMap<>();
		Map<String, MonitoredFile> filePathToMonitoredFiles = new ConcurrentHashMap<>();

		while (this.waitingForInitConfigs.size() > 0) {
			ExternalConfigurationConfig config = this.waitingForInitConfigs.remove(0);
			ExternalConfiguration instance = this.init(config, extensionManager);

			if (instance != null) {
				if (config.isMonitored()) {
					String filePath = config.getFilePath();
					if (!(filePath.startsWith("/") || filePath.startsWith("file:///")
							|| StringUtils.match(filePath, "[A-Za-z]:"))) {
						filePath = FileSystemUtils.createAbsolutePathFrom(
								System.getProperty("application.extensionsFolder", "extensions"),
								config.getExtensionName(), filePath);
					}
					MonitoredFile monitoredFile = new MonitoredFile.MonitoredFileBuilder().file(new File(filePath))
							.sensitivity(config.getSensitivity()).build();
					fileToInstanceName.putIfAbsent(monitoredFile, new HashSet<>());
					filePathToMonitoredFiles.putIfAbsent(filePath, monitoredFile);

					fileToInstanceName.get(monitoredFile).add(instance);
				}
			}
		}
		if (!fileToInstanceName.isEmpty()) {

			this.fileWatcher = new FileWatcher(fileToInstanceName.keySet(), new Callback<File>() {

				@Override
				public void apply(File changedFile) {
					MonitoredFile monitoredFile = filePathToMonitoredFiles.get(changedFile.getAbsolutePath());
					if (monitoredFile != null) {
						Set<ExternalConfiguration> collection = fileToInstanceName.get(monitoredFile);
						for (ExternalConfiguration instance : collection) {
							instance.update(changedFile);
						}
					}
				}
			});
		}
	}

	private ExternalConfiguration init(ExternalConfigurationConfig config, ExtensionManager extensionManager) {
		ExternalConfigurationParser parser = extensionManager.newInstance(config.getExtensionName(),
				config.getParser());
		if (parser != null) {
			ExternalConfigurationImpl instance = new ExternalConfigurationImpl(parser);
			this.instances.put(config.getName(), instance);
			return instance;
		}
		return null;
	}

	public void add(ExternalConfigurationConfig config) {
		if (config != null) {
			this.waitingForInitConfigs.add(config);
		}
	}

	public void stop() {
		if (this.fileWatcher != null) {
			this.fileWatcher.stop();
		}
	}
}
