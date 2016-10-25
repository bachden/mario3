package nhb.mario3.extension;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nhb.common.BaseLoggable;
import nhb.common.data.PuObjectRO;
import nhb.common.db.cassandra.CassandraDatasourceConfig;
import nhb.common.db.mongodb.config.MongoDBConfig;
import nhb.common.db.sql.SQLDataSourceConfig;
import nhb.common.utils.FileSystemUtils;
import nhb.mario3.config.HazelcastConfig;
import nhb.mario3.config.LifeCycleConfig;
import nhb.mario3.config.MessageProducerConfig;
import nhb.mario3.config.MonitorAgentConfig;
import nhb.mario3.config.RedisConfig;
import nhb.mario3.config.ZkClientConfig;
import nhb.mario3.config.gateway.GatewayConfig;
import nhb.mario3.config.serverwrapper.ServerWrapperConfig;

public final class ExtensionManager extends BaseLoggable {

	private String extensionsFolder = System.getProperty("application.extensionsFolder", "extensions");
	private Map<String, ExtensionLoader> extensionLoaderByName;
	private boolean loaded = false;

	public boolean isLoaded() {
		return this.loaded;
	}

	public void load(PuObjectRO globalProperties) throws Exception {
		File file = new File(FileSystemUtils.createAbsolutePathFrom(extensionsFolder));
		if (file.exists() && file.isDirectory()) {
			this.extensionLoaderByName = new HashMap<String, ExtensionLoader>();
			File[] children = file.listFiles();
			for (File ext : children) {
				if (ext.isDirectory() && !ext.getName().equalsIgnoreCase("__lib__")) {
					ExtensionLoader loader = new ExtensionLoader(ext);
					System.out.println("\t- Loading extension: " + ext.getName());
					loader.load(globalProperties.deepClone());
					this.extensionLoaderByName.put(loader.getName(), loader);
				}
			}
			this.loaded = true;
		} else {
			getLogger().error("Extension folder doesn't exists, path: {}", file.getAbsolutePath());
		}
	}

	public <T> T newInstance(String extensionName, String className) {
		if (extensionName != null && extensionName.trim().length() > 0) {
			ExtensionLoader loader = this.extensionLoaderByName.get(extensionName);
			if (loader != null) {
				if (className != null && className.trim().length() > 0) {
					try {
						return loader.newInstance(className.trim());
					} catch (Exception e) {
						getLogger().error("cannot create new instance for class name: {}, extension name: {}",
								className, extensionName, e);
					}
				} else {
					getLogger().error("class name cannot be empty");
				}
			} else {
				getLogger().error("Extension loader cannot be found");
			}
		} else {
			getLogger().error("no extension is loaded");
		}
		return null;
	}

	public List<SQLDataSourceConfig> getDataSourceConfigs() {
		if (this.isLoaded()) {
			List<SQLDataSourceConfig> results = new ArrayList<SQLDataSourceConfig>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getSQLDataSourceConfig() != null) {
					results.addAll(loader.getConfigReader().getSQLDataSourceConfig());
				}
			}
			return results;
		}
		return null;
	}

	public List<HazelcastConfig> getHazelcastConfigs() {
		if (this.isLoaded()) {
			List<HazelcastConfig> results = new ArrayList<HazelcastConfig>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getHazelcastConfigs() != null) {
					results.addAll(loader.getConfigReader().getHazelcastConfigs());
				}
			}
			return results;
		}
		return null;
	}

	public List<RedisConfig> getRedisConfigs() {
		if (this.isLoaded()) {
			List<RedisConfig> results = new ArrayList<RedisConfig>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getRedisConfigs() != null) {
					results.addAll(loader.getConfigReader().getRedisConfigs());
				}
			}
			return results;
		}
		return null;
	}

	public List<LifeCycleConfig> getLifeCycleConfigs() {
		if (this.isLoaded()) {
			List<LifeCycleConfig> results = new ArrayList<LifeCycleConfig>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getLifeCycleConfigs() != null) {
					results.addAll(loader.getConfigReader().getLifeCycleConfigs());
				}
			}
			return results;
		}
		return null;
	}

	public List<GatewayConfig> getGatewayConfigs() {
		if (this.isLoaded()) {
			List<GatewayConfig> results = new ArrayList<GatewayConfig>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getGatewayConfigs() != null) {
					results.addAll(loader.getConfigReader().getGatewayConfigs());
				}
			}
			return results;
		}
		return null;
	}

	public List<MongoDBConfig> getMongoDBConfigs() {
		if (this.isLoaded()) {
			List<MongoDBConfig> results = new ArrayList<MongoDBConfig>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getMongoDBConfigs() != null) {
					results.addAll(loader.getConfigReader().getMongoDBConfigs());
				}
			}
			return results;
		}
		return null;
	}

	public List<ServerWrapperConfig> getServerWrapperConfigs() {
		if (this.isLoaded()) {
			List<ServerWrapperConfig> results = new ArrayList<ServerWrapperConfig>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getServerWrapperConfigs() != null) {
					results.addAll(loader.getConfigReader().getServerWrapperConfigs());
				}
			}
			return results;
		}
		return null;
	}

	public Collection<MonitorAgentConfig> getMonitorAgentConfigs() {
		if (this.isLoaded()) {
			List<MonitorAgentConfig> results = new ArrayList<MonitorAgentConfig>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getMongoDBConfigs() != null) {
					results.addAll(loader.getConfigReader().getMonitorAgentConfigs());
				}
			}
			return results;
		}
		return null;
	}

	public Collection<MessageProducerConfig> getProducerConfigs() {
		if (this.isLoaded()) {
			List<MessageProducerConfig> results = new ArrayList<>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getServerWrapperConfigs() != null) {
					results.addAll(loader.getConfigReader().getProducerConfigs());

				}
			}
			return results;
		}
		return null;
	}

	public Collection<CassandraDatasourceConfig> getCassandraConfigs() {
		if (this.isLoaded()) {
			Collection<CassandraDatasourceConfig> results = new ArrayList<>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getCassandraConfigs() != null) {
					results.addAll(loader.getConfigReader().getCassandraConfigs());
				}
			}
			return results;
		}
		return null;
	}

	public Collection<ZkClientConfig> getListZkClientConfig() {
		if (this.isLoaded()) {
			Collection<ZkClientConfig> results = new ArrayList<>();
			for (ExtensionLoader loader : this.extensionLoaderByName.values()) {
				if (loader.getConfigReader().getZkClientConfigs() != null) {
					results.addAll(loader.getConfigReader().getZkClientConfigs());
				}
			}
			return results;
		}
		return null;
	}

	public PuObjectRO getExtensionProperty(String extensionName, String propertyName) {
		return this.extensionLoaderByName.get(extensionName).getConfigReader().getProperty(propertyName);
	}
}
