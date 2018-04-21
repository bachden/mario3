package com.mario.extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.mario.contact.ContactBook;
import com.mario.external.configuration.ExternalConfigurationManager;
import com.mario.schedule.distributed.impl.config.HzDistributedSchedulerConfigManager;
import com.mario.services.ServiceManager;
import com.mario.zeromq.ZMQSocketRegistryManager;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.FileSystemUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ExtensionLoader extends BaseLoggable {

	private static final String[] REGEX_SPECIAL_CHARS = new String[] { ".", "*", "-", "[", "]", "(", ")", "$", "^" };

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

	public static void main(String[] args) {
		String str = "<a><b>${mario.inject.[mysql-user]}</b><c>${mario.inject.[mysql-user]}</c><d>${mario.inject.mysql-pass*}</d></a>";
		Properties props = new Properties();
		props.put("mario.inject.[mysql-user]", "root");
		props.put("mario.inject.mysql-pass*", "123456");

		str = findAndReplace(str, props);
		System.out.println(str);
	}

	private static final String normalizeKey(String key) {
		String result = key;
		for (String c : REGEX_SPECIAL_CHARS) {
			result = result.replaceAll("\\" + c, "\\\\" + c);
		}
		return result;
	}

	private static final String findAndReplace(String source, Properties variables) {
		if (source == null) {
			return null;
		}
		
		if (variables == null) {
			return source;
		}
		
		String result = source;
		for (Object keyObj : variables.keySet()) {
			String key = keyObj.toString();
			String variableKey = "\\$\\{" + normalizeKey(key) + "\\}";
			String variableValue = variables.getProperty(key);
			result = result.replaceAll(variableKey, variableValue);
			log.info("Injected {}={}", variableKey, variableValue);
		}
		return result;
	}

	public void load(PuObjectRO globalProperties, ContactBook contactBook, ServiceManager serviceManager,
			ExternalConfigurationManager externalConfigurationManager,
			HzDistributedSchedulerConfigManager hzDistributedSchedulerConfigManager,
			ZMQSocketRegistryManager zmqSocketRegistryManager) throws Exception {
		if (extFolder.exists() && extFolder.isDirectory()) {
			// read config
			System.out.println("\t\t- Reading config file");
			this.configReader = new ExtensionConfigReader(globalProperties, contactBook, serviceManager,
					externalConfigurationManager, hzDistributedSchedulerConfigManager, zmqSocketRegistryManager);

			Properties variables = null;
			String variablesFilePath = FileSystemUtils.createPathFrom(extFolder.getAbsolutePath(),
					"variables.properties");
			File variablesFile = new File(variablesFilePath);
			if (variablesFile.exists() && variablesFile.isFile()) {
				variables = new Properties();
				try (InputStream is = new FileInputStream(variablesFile)) {
					variables.load(is);
				} catch (Exception e) {
					getLogger().warn("Cannot read variables.properties from path: {}" + variablesFilePath, e);
				}
			}

			String extensionXmlFilePath = FileSystemUtils.createPathFrom(extFolder.getAbsolutePath(), "extension.xml");
			File extensionXmlFile = new File(extensionXmlFilePath);
			if (extensionXmlFile.exists() && extensionXmlFile.isFile()) {

				try (InputStream is = new FileInputStream(extensionXmlFile); StringWriter sw = new StringWriter()) {
					IOUtils.copy(is, sw);

					getLogger().info("Processing variable injection for file " + extensionXmlFilePath);
					String extensionXml = findAndReplace(sw.toString(), variables);

					this.configReader.readXml(extensionXml);
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
									throw new RuntimeException(
											"error while getting url for jar file " + jar.getAbsolutePath(), e);
								}
							}
							this.classLoader = new URLClassLoader(urls);
						}
					}
				}
			} else {
				throw new FileNotFoundException("Cannot read extension.xml file from path: " + extensionXmlFilePath);
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
