package com.mario.extension;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import com.mario.contact.ContactBook;
import com.mario.external.configuration.ExternalConfigurationManager;
import com.mario.schedule.distributed.impl.config.HzDistributedSchedulerConfigManager;
import com.mario.services.ServiceManager;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.FileSystemUtils;

import lombok.Getter;

public final class ExtensionLoader extends BaseLoggable {

	private ExtensionConfigReader configReader;
	private File extFolder;
	@Getter
	private ClassLoader classLoader;
	private String name;

	ExtensionLoader(File extFolder) {
		this.extFolder = extFolder;
	}

	ExtensionLoader(String path) {
		this(new File(path));
	}

	public void load(PuObjectRO globalProperties, ContactBook contactBook, ServiceManager serviceManager,
			ExternalConfigurationManager externalConfigurationManager,
			HzDistributedSchedulerConfigManager hzDistributedSchedulerConfigManager) throws Exception {
		if (extFolder.exists() && extFolder.isDirectory()) {
			// read config
			System.out.println("\t\t- Reading config file");
			this.configReader = new ExtensionConfigReader(globalProperties, contactBook, serviceManager,
					externalConfigurationManager, hzDistributedSchedulerConfigManager);
			this.configReader.read(FileSystemUtils.createPathFrom(extFolder.getAbsolutePath(), "extension.xml"));
			this.name = configReader.getExtensionName();

			// load jar files
			System.out.println("\t\t- Loading jar files");
			File libFolder = new File(extFolder.getAbsolutePath(), "lib");
			if (libFolder.exists() && libFolder.isDirectory()) {
				List<File> jars = FileSystemUtils.scanFolder(libFolder);
				if (jars != null && jars.size() > 0) {
					URL[] urls = new URL[jars.size()];
					for (int i = 0; i < jars.size(); i++) {
						File jar = jars.get(i);
						try {
							urls[i] = jar.toURI().toURL();
						} catch (MalformedURLException e) {
							throw new RuntimeException("error while getting url for jar file " + jar.getAbsolutePath(),
									e);
						}
					}
					this.classLoader = new URLClassLoader(urls);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <ClassType> ClassType loadClass(String className) throws ClassNotFoundException {
		if (className != null && className.trim().length() > 0) {
			Class<?> clazz = this.classLoader == null ? null : this.classLoader.loadClass(className);
			if (clazz == null) {
				clazz = this.getClass().getClassLoader().loadClass(className);
			}
			if (clazz == null) {
				clazz = this.getClass().getClassLoader().getParent().loadClass(className);
			}
			return (ClassType) clazz;
		}
		return null;
	}

	public <T> T newInstance(String className) throws Exception {
		Class<T> clazz = this.loadClass(className);
		if (clazz != null) {
			return clazz.newInstance();
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public ExtensionConfigReader getConfigReader() {
		return this.configReader;
	}
}
