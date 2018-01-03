package com.mario.external.configuration.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.mario.config.ExternalConfigurationConfig;
import com.mario.extension.ExtensionLoader;
import com.mario.external.configuration.ExternalConfigurationParser;
import com.mario.statics.ExtensionLoaderAware;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuObject;

import lombok.Setter;

public class YamlFileParser implements ExternalConfigurationParser, Loggable, ExtensionLoaderAware {

	private PuObject initParams;

	@Override
	public void init(PuObject initParams) {
		this.initParams = initParams == null ? new PuObject() : initParams;
	}

	@Setter
	private ExtensionLoader extensionLoader;

	@Setter
	private ExternalConfigurationConfig config;

	@Override
	public Object parse(InputStream inputStream) {
		try {
			String wrapperClassName = this.initParams.getString("wrapperClass", null);
			return this.parse(inputStream,
					wrapperClassName == null ? null : this.extensionLoader.loadClass(wrapperClassName));
		} catch (Exception e) {
			throw new RuntimeException("An error occurs while parsing yaml config from input stream", e);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T parse(InputStream inputStream, Class<T> wrapperClass) throws Exception {
		Class<YamlReader> yamlReaderClass = this.extensionLoader.loadClass(YamlReader.class.getName());
		Class<?> argumentClass = this.extensionLoader.loadClass(Reader.class.getName());
		Constructor<YamlReader> constructor = yamlReaderClass.getConstructor(argumentClass);

		Class<?> inputStreamReaderClass = this.extensionLoader.loadClass(InputStreamReader.class.getName());

		Object inputStreamReader = inputStreamReaderClass.getConstructor(InputStream.class, Charset.class)
				.newInstance(inputStream, Charset.forName("utf-8"));
		YamlReader reader = constructor.newInstance(inputStreamReader);
		reader.getConfig().readConfig.setClassLoader(this.extensionLoader.getClassLoader());
		return wrapperClass == null ? (T) reader.read() : reader.read(wrapperClass);
	}
}
