package com.mario.ssl;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.mario.config.SSLContextConfig;
import com.nhb.common.Loggable;
import com.nhb.common.utils.FileSystemUtils;

public final class SSLContextManager implements Loggable {

	private final Map<String, SSLContext> contexts = new ConcurrentHashMap<>();

	public void init(Collection<SSLContextConfig> configs) {
		for (SSLContextConfig config : configs) {
			SSLContext serverContext = null;
			try {
				final String keyStoreFilePath = FileSystemUtils.createAbsolutePathFrom(
						System.getProperty("application.extensionsFolder", "extensions"), config.getExtensionName(),
						config.getFilePath());

				final String keyStoreFilePassword = config.getPassword();
				final KeyStore ks = KeyStore.getInstance(config.getFormat());

				try (final FileInputStream fin = new FileInputStream(keyStoreFilePath)) {
					if (keyStoreFilePassword != null) {
						ks.load(fin, keyStoreFilePassword.toCharArray());
					} else {
						ks.load(fin, "".toCharArray());
					}
				}

				final KeyManagerFactory kmf = config.getAlgorithm().equalsIgnoreCase("RSA")
						? KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
						: KeyManagerFactory.getInstance(config.getAlgorithm());
				kmf.init(ks, keyStoreFilePassword.toCharArray());

				serverContext = SSLContext.getInstance(config.getProtocol());
				serverContext.init(kmf.getKeyManagers(), null, null);

				contexts.put(config.getName(), serverContext);
			} catch (Exception e) {
				throw new Error("Failed to initialize the SSLContext named " + config.getName() + ", extension "
						+ config.getExtensionName(), e);
			}
		}
	}

	public SSLContext getSSLContext(String name) {
		if (name == null) {
			// getLogger().error("SSL Context Name cannot be null", new NullPointerException());
			return null;
		}
		return this.contexts.get(name);
	}
}
