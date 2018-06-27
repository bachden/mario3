package com.mario.zookeeper;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.proto.WatcherEvent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mario.config.ZkClientConfig;
import com.mario.extension.ExtensionManager;
import com.nhb.common.BaseLoggable;

public class ZooKeeperClientManager extends BaseLoggable implements Closeable {

	private final ExecutorService executor = Executors
			.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("ZooKeeper Watcher Thread #%d").build());

	private final ExtensionManager entityManager;

	private final Map<String, Set<Watcher>> zooKeeperNameToWatchers = new ConcurrentHashMap<>();

	private final Map<String, ZooKeeper> zooKeeperClients = new ConcurrentHashMap<>();
	private final Map<String, ZkClient> zkClients = new ConcurrentHashMap<>();
	private final Map<String, ZkClientConfig> zooKeeperConfigs = new ConcurrentHashMap<>();

	public ZooKeeperClientManager(ExtensionManager extensionManager) {
		if (extensionManager == null) {
			throw new NullPointerException("Entity manager instance cannot be null");
		}
		this.entityManager = extensionManager;
	}

	public void prepareConfigs(Collection<ZkClientConfig> configs) {
		if (configs != null) {
			for (ZkClientConfig config : configs) {
				this.zooKeeperConfigs.put(config.getName(), config);
			}
		}
	}

	public ZooKeeper acquireZooKeeper(String name) {
		if (!this.zooKeeperClients.containsKey(name) && this.zooKeeperConfigs.containsKey(name)) {
			synchronized (this) {
				if (!this.zooKeeperClients.containsKey(name)) {
					try {
						this.zooKeeperClients.put(name, this.initZooKeeper(this.zooKeeperConfigs.get(name)));
					} catch (Exception e) {
						throw new ZooKeeperException("ZooKeeper exception, name: " + name + ", extension: "
								+ this.zooKeeperConfigs.get(name).getExtensionName(), e);
					}
				}
			}
		}
		return this.zooKeeperClients.get(name);
	}

	public void addWatcher(String zooKeeperName, Watcher watcher) {
		if (!this.zooKeeperClients.containsKey(zooKeeperName)
				|| !this.zooKeeperNameToWatchers.containsKey(zooKeeperName)) {
			throw new IllegalStateException(
					"ZooKeeper for name " + zooKeeperName + " was not initialized, cannot to add watcher");
		}
		if (watcher != null && !this.zooKeeperNameToWatchers.get(zooKeeperName).contains(watcher)) {
			this.zooKeeperNameToWatchers.get(zooKeeperName).add(watcher);
		}
	}

	public ZkClient acquireZkClient(final String name) {
		if (!this.zkClients.containsKey(name) && this.zooKeeperConfigs.containsKey(name)) {
			synchronized (this) {
				if (!this.zkClients.containsKey(name)) {
					this.zkClients.put(name, this.initZkClient(this.zooKeeperConfigs.get(name)));
				}
			}
		}
		return this.zkClients.get(name);
	}

	private ZkClient initZkClient(ZkClientConfig config) {
		if (config.getServers() == null || config.getServers().trim().length() == 0) {
			throw new IllegalArgumentException("ZkClientConfig must contains non-empty servers, config name: "
					+ config.getName() + ", extension: " + config.getExtensionName());
		}
		return new ZkClient(config.getServers(), config.getSessionTimeout(), config.getConnectionTimeout(),
				this.entityManager.newInstance(config.getExtensionName(), config.getSerializerClass()),
				config.getOperationRetryTimeout());
	}

	private ZooKeeper initZooKeeper(final ZkClientConfig config) throws Exception {
		if (config.getServers() == null || config.getServers().trim().length() == 0) {
			throw new IllegalArgumentException("ZkClientConfig must contains non-empty servers, config name: "
					+ config.getName() + ", extension: " + config.getExtensionName());
		}
		this.zooKeeperNameToWatchers.put(config.getName(), new CopyOnWriteArraySet<>());
		CountDownLatch connectedSignal = new CountDownLatch(1);
		ZooKeeper zookeeper = new ZooKeeper(config.getServers(), config.getSessionTimeout(), new Watcher() {

			private final String zooKepperName = config.getName();

			@Override
			public void process(WatchedEvent watchedEvent) {
				if (connectedSignal.getCount() > 0 && KeeperState.SyncConnected.equals(watchedEvent.getState())) {
					connectedSignal.countDown();
				}
				if (zooKeeperNameToWatchers.containsKey(this.zooKepperName)) {
					WatcherEvent wrapper = watchedEvent.getWrapper();
					for (Watcher watcher : zooKeeperNameToWatchers.get(this.zooKepperName)) {
						executor.execute(new Runnable() {
							public void run() {
								watcher.process(new WatchedEvent(wrapper));
							}
						});
					}
				}
			}
		});
		if (!connectedSignal.await(15, TimeUnit.SECONDS)) {
			zookeeper.close();
			throw new TimeoutException("Cannot connect zooKeeper after 15 seconds");
		}
		return zookeeper;
	}

	@Override
	public void close() {
		if (!this.executor.isShutdown()) {
			this.executor.shutdown();
			try {
				if (this.executor.awaitTermination(6, TimeUnit.SECONDS)) {
					this.executor.shutdownNow();
				}
			} catch (Exception e) {
				getLogger().error("Shutdown watcher callback executor error", e);
			}
		}
		for (Entry<String, ZkClient> entry : this.zkClients.entrySet()) {
			try {
				entry.getValue().close();
			} catch (Exception e) {
				getLogger().error("Cannot close zkClient named {}", entry.getKey(), e);
			}
		}
		for (Entry<String, ZooKeeper> entry : this.zooKeeperClients.entrySet()) {
			try {
				entry.getValue().close();
			} catch (Exception e) {
				getLogger().error("Cannot close ZooKeeper named {}", entry.getKey(), e);
			}
		}
	}
}
