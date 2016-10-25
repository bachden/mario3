package nhb.mario3.cache;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import nhb.common.BaseLoggable;
import nhb.common.cache.jedis.JedisService;
import nhb.common.utils.FileSystemUtils;
import nhb.common.vo.HostAndPort;
import nhb.mario3.cache.hazelcast.HazelcastInitializer;
import nhb.mario3.config.HazelcastConfig;
import nhb.mario3.config.RedisConfig;
import nhb.mario3.entity.EntityManager;
import nhb.mario3.entity.HazelcastConfigPreparer;
import nhb.mario3.entity.LifeCycle;
import nhb.mario3.extension.ExtensionManager;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

@SuppressWarnings("deprecation")
public class CacheManager extends BaseLoggable {

	private Map<String, HazelcastInstance> hazelcastInstances;
	private Map<String, JedisService> jedisServices;

	private ExtensionManager extensionManager;

	private final Map<String, HazelcastConfig> lazyInitConfigs = new ConcurrentHashMap<>();

	public CacheManager(ExtensionManager extMan) {
		this.hazelcastInstances = new HashMap<>();
		this.jedisServices = new HashMap<>();
		this.extensionManager = extMan;
	}

	public void stop() {
		for (HazelcastInstance instance : this.hazelcastInstances.values()) {
			try {
				instance.shutdown();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		for (JedisService jedisService : this.jedisServices.values()) {
			try {
				jedisService.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	private JedisService createJedisService(RedisConfig config) {
		JedisService jedisService = new JedisService();
		switch (config.getRedisType()) {
		case SINGLE:
			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(config.getPoolSize());
			JedisPool pool = new JedisPool(poolConfig, config.getFirstEndpoint().getHost(),
					config.getFirstEndpoint().getPort(), config.getTimeout(), config.getPassword());
			jedisService.setPool(pool);
			break;
		case CLUSTER:
			Set<redis.clients.jedis.HostAndPort> jedisClusterNodes = new HashSet<redis.clients.jedis.HostAndPort>();
			for (HostAndPort hnp : config.getEndpoints()) {
				jedisClusterNodes.add(new redis.clients.jedis.HostAndPort(hnp.getHost(), hnp.getPort()));
			}
			JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes);
			jedisService.setCluster(jedisCluster);
			break;
		case SENTINEL:
			Set<String> sentinels = new HashSet<>();
			for (HostAndPort hnp : config.getEndpoints()) {
				sentinels.add(hnp.toString());
			}
			JedisSentinelPool sentinelPool = new JedisSentinelPool(config.getMasterName(), sentinels);
			jedisService.setSentinel(sentinelPool);
			break;
		case MASTER_SLAVE:
			break;
		default:
			break;
		}
		return jedisService;
	}

	private void initHazelcast(HazelcastConfig config, HazelcastInitializer initializer) {
		if (config.isMember()) {
			try {
				Config hazelcastConfig = null;
				if (config.getConfigFilePath() != null) {
					hazelcastConfig = new XmlConfigBuilder(FileSystemUtils.createAbsolutePathFrom(
							System.getProperty("application.extensionsFolder", "extensions"), config.getExtensionName(),
							config.getConfigFilePath())).build();
				}
				if (initializer == null && config.getInitializerClass() != null) {
					if (hazelcastConfig == null) {
						hazelcastConfig = new Config();
					}
					initializer = this.extensionManager.newInstance(config.getExtensionName(),
							config.getInitializerClass());
				}
				if (initializer != null) {
					initializer.prepare(hazelcastConfig);
				}
				hazelcastInstances.put(config.getName(), hazelcastConfig == null ? Hazelcast.newHazelcastInstance()
						: Hazelcast.newHazelcastInstance(hazelcastConfig));
			} catch (Exception e) {
				getLogger().error("init hazelcast config error: ", e);
			}
		} else {
			try {
				ClientConfig hazelcastConfig = new XmlClientConfigBuilder(FileSystemUtils.createAbsolutePathFrom(
						System.getProperty("application.extensionsFolder", "extensions"), config.getExtensionName(),
						config.getConfigFilePath())).build();

				hazelcastInstances.put(config.getName(), HazelcastClient.newHazelcastClient(hazelcastConfig));
			} catch (Exception e) {
				getLogger().error("init hazelcast config error: ", e);
			}
		}
	}

	/**
	 * Auto init all the caching instances, include hazelcast and redis. <br/>
	 * Every Hazelcast not marked as "lazyInit" will be initialized here,
	 * otherwise, it will be stored to be init later in autoInitLazyHazelcasts
	 * method
	 * 
	 * @param hazelcastConfigs
	 * @param redisConfigs
	 */
	public void init0(List<HazelcastConfig> hazelcastConfigs, List<RedisConfig> redisConfigs) {

		if (redisConfigs != null && redisConfigs.size() > 0) {
			redisConfigs.forEach(config -> {
				// jedis service
				this.jedisServices.put(config.getName(), createJedisService(config));
			});
		}

		if (hazelcastConfigs != null && hazelcastConfigs.size() > 0) {
			hazelcastConfigs.parallelStream().forEach(config -> {
				if (config.isLazyInit() && config.isMember()) {
					this.lazyInitConfigs.put(config.getName(), config);
				} else {
					this.initHazelcast(config, null);
				}
			});
		}
	}

	public void autoInitLazyHazelcasts(EntityManager entityManager) {
		for (HazelcastConfig config : this.lazyInitConfigs.values()) {
			if (!this.hazelcastInstances.containsKey(config.getName())) {
				if (config.isAutoInitOnExtensionReady() && config.isMember()) {
					Config hazelcastConfig = null;
					if (config.getConfigFilePath() != null) {
						try {
							hazelcastConfig = new XmlConfigBuilder(FileSystemUtils.createAbsolutePathFrom(
									System.getProperty("application.extensionsFolder", "extensions"),
									config.getExtensionName(), config.getConfigFilePath())).build();
						} catch (FileNotFoundException e) {
							throw new RuntimeException("Hazelcast config file not found", e);
						}
					} else {
						hazelcastConfig = new Config();
					}
					for (String handleName : config.getInitializers()) {
						LifeCycle entity = entityManager.getLifeCycle(handleName);
						if (entity instanceof HazelcastConfigPreparer) {
							((HazelcastConfigPreparer) entity).prepareHazelcastConfig(config.getName(),
									hazelcastConfig);
						} else {
							getLogger().warn(
									"the lifecycle named '{}' is not instanceof HazelcastConfigPreparer, cannot use to prepare hazelcast instance '{}', Extension: {}",
									entity == null ? "null" : entity.getName(), config.getName(),
									config.getExtensionName());
						}
					}
					hazelcastInstances.put(config.getName(), hazelcastConfig == null ? Hazelcast.newHazelcastInstance()
							: Hazelcast.newHazelcastInstance(hazelcastConfig));
				}
			} else {
				if (config.isLazyInit()) {
					getLogger().warn("", new IllegalStateException("Hazelcast named '" + config.getName()
							+ "' in extension '" + config.getExtensionName()
							+ "' config has been marked as lazy init, but somewhere has been acquire it via MarioAPI before, check your code"));
				}
			}
		}
	}

	public HazelcastInstance getHazelcastInstance(String name) {
		if (this.hazelcastInstances.containsKey(name)) {
			return this.hazelcastInstances.get(name);
		} else if (this.lazyInitConfigs.containsKey(name)) {
			this.initHazelcast(this.lazyInitConfigs.get(name), null);
			return this.hazelcastInstances.get(name);
		}
		return null;
	}

	/**
	 * this method will be removed in the near future use initializer config in
	 * extension.xml
	 * 
	 * @param name
	 * @param initializer
	 * @return
	 */
	@Deprecated
	public HazelcastInstance getHazelcastInstance(String name, HazelcastInitializer initializer) {
		if (this.hazelcastInstances.containsKey(name)) {
			if (initializer != null) {
				getLogger().warn("", new IllegalStateException("You may want to init hazelcast named '" + name
						+ "' before it getting created, but somewhere has been acquire this hazelcast and it has already created, check your code please."));
			}
			return this.hazelcastInstances.get(name);
		} else if (this.lazyInitConfigs.containsKey(name)) {
			this.initHazelcast(this.lazyInitConfigs.get(name), initializer);
			return this.hazelcastInstances.get(name);
		}
		return null;
	}

	public JedisService getJedisServiceByName(String name) {
		return this.jedisServices.get(name);
	}

}
