package com.mario.external.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mario.config.ExternalConfigurationConfig;
import com.mario.config.ExternalConfigurationConfig.ExternalConfigurationParserConfig;
import com.mario.extension.ExtensionManager;
import com.mario.external.configuration.parser.RawFileParser;
import com.mario.statics.ExtensionLoaderAware;
import com.nhb.common.Loggable;
import com.nhb.common.async.Callback;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.common.utils.StringUtils;

public class ExternalConfigurationManager implements Loggable {

	private FileWatcher fileWatcher;
	private final Map<String, ExternalConfiguration> instances = new ConcurrentHashMap<>();

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
				String filePath = config.getFilePath();
				if (!(filePath.startsWith("/") || filePath.startsWith("file:///")
						|| StringUtils.match(filePath, "[A-Za-z]:"))) {
					filePath = FileSystemUtils.createAbsolutePathFrom(
							System.getProperty("application.extensionsFolder", "extensions"), config.getExtensionName(),
							filePath);
				}
				if (config.isMonitored()) {
					getLogger().debug("Monitor file: " + filePath);
					MonitoredFile monitoredFile = new MonitoredFile.MonitoredFileBuilder().file(new File(filePath))
							.sensitivity(config.getSensitivity()).build();
					fileToInstanceName.putIfAbsent(monitoredFile, new HashSet<>());
					filePathToMonitoredFiles.putIfAbsent(filePath, monitoredFile);

					fileToInstanceName.get(monitoredFile).add(instance);
				}
				instance.update(new File(filePath));
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
							try {
								instance.update(changedFile);
							} catch (Exception e) {
								getLogger().warn("Error while update changed configuration file", e);
							}
						}
					} else {
						getLogger().debug("MonitoredFile mapping to {} cannot be found", changedFile.getAbsolutePath());
					}
				}
			});
			this.fileWatcher.start();
		}
	}

	private ExternalConfiguration init(ExternalConfigurationConfig config, ExtensionManager extensionManager) {
		ExternalConfigurationParserConfig parserConfig = config.getParserConfig();
		String parserHandler = parserConfig == null ? null : parserConfig.getHandler();

		ExternalConfigurationParser parser = parserHandler == null ? new RawFileParser()
				: extensionManager.newInstance(config.getExtensionName(), parserHandler);
		if (parser != null) {
			if (parser instanceof ExtensionLoaderAware) {
				((ExtensionLoaderAware) parser)
						.setExtensionLoader(extensionManager.getExtensionLoader(config.getExtensionName()));
			}
			parser.init(parserConfig.getInitParams());
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
