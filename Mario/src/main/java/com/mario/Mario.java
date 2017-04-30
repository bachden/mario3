package com.mario;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.IOUtils;
import org.nhb.core.thread.DeadLockMonitor;

import com.mario.api.MarioApiFactory;
import com.mario.cache.CacheManager;
import com.mario.config.LifeCycleConfig;
import com.mario.config.MessageHandlerConfig;
import com.mario.contact.ContactBook;
import com.mario.entity.EntityManager;
import com.mario.extension.ExtensionManager;
import com.mario.gateway.Gateway;
import com.mario.gateway.GatewayEvent;
import com.mario.gateway.GatewayManager;
import com.mario.gateway.serverwrapper.ServerWrapperManager;
import com.mario.monitor.MonitorAgentManager;
import com.mario.producer.MessageProducerManager;
import com.mario.schedule.impl.SchedulerFactory;
import com.mario.services.ServiceManager;
import com.mario.ssl.SSLContextManager;
import com.mario.zookeeper.ZooKeeperClientManager;
import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.exception.InvalidDataException;
import com.nhb.common.db.cassandra.CassandraDatasourceManager;
import com.nhb.common.db.mongodb.MongoDBSourceManager;
import com.nhb.common.db.sql.SQLDataSourceConfig;
import com.nhb.common.db.sql.SQLDataSourceManager;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.common.utils.Initializer;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventHandler;

public final class Mario extends BaseLoggable {

	static {
		Initializer.bootstrap(Mario.class);
	}

	public static void main(String[] args) {
		final Mario app = new Mario();
		final CountDownLatch shutdownSignal = new CountDownLatch(1);
		Thread keepAliveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					shutdownSignal.await();
				} catch (InterruptedException e) {

				}
			}
		}, "KeepAliveThread");
		try {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				{
					this.setName("Shutdown Thread");
					this.setPriority(MAX_PRIORITY);
				}

				@Override
				public void run() {
					try {
						app.stop();
					} catch (Exception e) {
						e.printStackTrace();
					}
					shutdownSignal.countDown();
				}
			});

			app.start();
			keepAliveThread.start();
		} catch (Exception e) {
			System.err.println("error while starting application: ");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private Mario() {

	}

	private boolean running = false;
	private ExtensionManager extensionManager;
	private GatewayManager gatewayManager;
	private EntityManager entityManager;
	private CassandraDatasourceManager cassandraDatasourceManager;
	private SQLDataSourceManager sqlDataSourceManager;
	private MarioApiFactory apiFactory;
	private SchedulerFactory schedulerFactory;
	private CacheManager cacheManager;
	private ServerWrapperManager serverWrapperManager;
	private MongoDBSourceManager mongoDBSourceManager;
	private MonitorAgentManager monitorAgentManager;
	private MessageProducerManager producerManager;
	private ZooKeeperClientManager zkClientManager;
	private SSLContextManager sslContextManager;

	private ServiceManager serviceManager;
	private ContactBook contactBook;

	private final PuObject globalProperties = new PuObject();

	private void start() throws Exception {
		if (this.running) {
			throw new IllegalAccessException("Application is already running");
		}

		this.readGlobalProperties();

		for (Entry<String, PuValue> entry : this.globalProperties) {
			if (entry.getValue().getType() != PuDataType.PUOBJECT) {
				throw new InvalidDataException("Child-value of globalProperties must be PuObject");
			}
		}

		System.out.println("create contack book");
		this.contactBook = new ContactBook();

		System.out.println("create service manager");
		this.serviceManager = new ServiceManager();

		getLogger().debug("Global properties: " + this.globalProperties);

		System.out.println("create scheduler factory");
		this.schedulerFactory = SchedulerFactory.getInstance();

		System.out.println("create mongodb source manager");
		this.mongoDBSourceManager = new MongoDBSourceManager();

		System.out.println("create server wrapper manager");
		this.serverWrapperManager = new ServerWrapperManager();

		System.out.println("create producer manager");
		this.producerManager = new MessageProducerManager(this.serverWrapperManager);

		System.out.println("create sql data source manager");
		this.sqlDataSourceManager = new SQLDataSourceManager();

		System.out.println("Create SSLContextManager");
		this.sslContextManager = new SSLContextManager();

		System.out.println("create extension manager");
		this.extensionManager = new ExtensionManager();

		System.out.println("create cache manager");
		this.cacheManager = new CacheManager(this.extensionManager);

		System.out.println("Loading extension...");
		this.extensionManager.load(this.globalProperties, this.contactBook, this.serviceManager);

		System.out.println("Init SSLContexts");
		this.sslContextManager.init(this.extensionManager.getSSLContextConfigs());

		System.out.println("Create zkClientManager");
		this.zkClientManager = new ZooKeeperClientManager(this.extensionManager);

		System.out.println("Prepare zookeeper client config");
		this.zkClientManager.prepareConfigs(extensionManager.getListZkClientConfig());

		System.out.println("create cassandra datasource manager");
		this.cassandraDatasourceManager = new CassandraDatasourceManager();

		System.out.println("Create gateway manager");
		this.gatewayManager = new GatewayManager(this.extensionManager, this.serverWrapperManager,
				this.sslContextManager);

		System.out.println("Add configs to mongoDB data source manager");
		this.mongoDBSourceManager.addConfigs(this.extensionManager.getMongoDBConfigs());

		System.out.println("Create API factory");
		this.apiFactory = new MarioApiFactory(this.sqlDataSourceManager, this.cassandraDatasourceManager,
				this.schedulerFactory, this.mongoDBSourceManager, this.cacheManager, this.monitorAgentManager,
				this.producerManager, this.gatewayManager, this.zkClientManager, this.extensionManager,
				this.serverWrapperManager, this.globalProperties, this.contactBook, this.serviceManager);

		System.out.println("Init service mananger");
		serviceManager.init(this.apiFactory);

		System.out.println("create monitor agent");
		this.monitorAgentManager = new MonitorAgentManager(apiFactory, this.entityManager);

		System.out.println("Register sql datasource config");
		for (SQLDataSourceConfig dataSourceConfig : this.extensionManager.getDataSourceConfigs()) {
			this.sqlDataSourceManager.registerDataSource(dataSourceConfig.getName(), dataSourceConfig.getProperties());
		}

		System.out.println("Init cassandra datasource manager by loaded configs");
		this.cassandraDatasourceManager.init(this.extensionManager.getCassandraConfigs());

		System.out.println("init CacheManager with hazelcast configs and redis configs");
		cacheManager.init0(extensionManager.getHazelcastConfigs(), extensionManager.getRedisConfigs());

		System.out.println("Init Server wrapper manager");
		this.serverWrapperManager.init(extensionManager.getServerWrapperConfigs());
		System.out.println("Start server wrappers");
		this.serverWrapperManager.start();

		System.out.println("Init producer manager");
		this.producerManager.init(extensionManager.getProducerConfigs());
		System.out.println("Start producer manager");
		this.producerManager.start();

		System.out.println("Init gateway manager");
		this.gatewayManager.init(extensionManager.getGatewayConfigs());

		System.out.println("Create entityManager");
		this.entityManager = new EntityManager(this.extensionManager, this.apiFactory);
		System.out.println("Add initComplete event listener on entityManager");
		this.entityManager.addEventListener("initComplete", new EventHandler() {

			@Override
			public void onEvent(Event event) throws Exception {
				System.out.println("entity init complete");
				entityManager.removeEventListener("initComplete", this);
				cacheManager.autoInitLazyHazelcasts(entityManager);
			}
		});

		System.out.println("Init entity manager");
		this.entityManager.init();

		System.out.println("Init monitor agent");
		this.monitorAgentManager.init(extensionManager.getMonitorAgentConfigs());

		System.out.println("Setting gateway manager to apiFactory");
		this.apiFactory.setGatewayManager(this.gatewayManager);
		System.out.println("Binding gateway to handlers");
		this.bindGatewayToHandlers();

		System.out.println("Add allGatewayReady event listener on gatewayManager");
		this.gatewayManager.addEventListener(GatewayEvent.ALL_GATEWAY_READY, new EventHandler() {

			@Override
			public void onEvent(Event event) throws Exception {
				System.out.println("All Gateway ready");
				gatewayManager.removeEventListener(GatewayEvent.ALL_GATEWAY_READY, this);
				entityManager.dispatchServerReady();
			}
		});

		System.out.println("Start all gateways");
		this.gatewayManager.start();

		System.out.println("Start monitor agent");
		this.monitorAgentManager.start();

		this.running = true;

		System.out.println("Start deadlock monitor");
		this.startDeadlockMonitoring();
	}

	private void readGlobalProperties() {
		String filePath = System.getProperty("server.globalProperties.file",
				"conf" + File.separator + "global-properties.xml");
		if (!(filePath.startsWith(File.separator) || filePath.charAt(1) == ':')) {
			filePath = FileSystemUtils.createAbsolutePathFrom(filePath);
		}

		try (InputStream is = new FileInputStream(filePath); StringWriter sw = new StringWriter()) {
			IOUtils.copy(is, sw);
			PuObject puo = PuObject.fromXML(sw.toString());
			this.globalProperties.addAll(puo);
		} catch (Exception e) {
			getLogger().info("Read default server properties file error", e);
		}
	}

	private void startDeadlockMonitoring() {
		// start dead lock monitoring
		DeadLockMonitor deadLockMonitor = new DeadLockMonitor();
		deadLockMonitor.addListener(new DeadLockMonitor.Listener() {

			@Override
			public void deadlockDetected(ThreadInfo paramThreadInfo) {
				getLogger().error("******** Deadlock detected ********\n" + paramThreadInfo.toString());
			}

			@Override
			public void thresholdExceeded(ThreadInfo[] paramArrayOfThreadInfo) {
				getLogger().warn(
						"******** Max thread threshold exeeded ********\n" + Arrays.asList(paramArrayOfThreadInfo));
			}
		});
	}

	private void bindGatewayToHandlers() {
		this.entityManager.getMessageHandlers().forEach(handler -> {
			LifeCycleConfig config = entityManager.getConfig(handler.getName());
			if (config instanceof MessageHandlerConfig) {
				((MessageHandlerConfig) config).getBindingGateways().forEach(gatewayName -> {
					Gateway gateway = gatewayManager.getGatewayByName(gatewayName);
					if (gateway == null) {
						throw new NullPointerException("Gateway can not be found for name: " + gatewayName);
					} else {
						handler.bind(gateway);
					}
				});
			}
		});
	}

	private void stop() {
		if (this.schedulerFactory != null) {
			System.out.print("Stopping scheduler factory... ");
			this.schedulerFactory.stop();
			System.out.println("DONE");
		}
		if (this.producerManager != null) {
			System.out.print("Stopping producer manager... ");
			this.producerManager.stop();
			System.out.println("DONE");
		}
		if (this.serverWrapperManager != null) {
			System.out.print("Stopping server wrappers... ");
			this.serverWrapperManager.stop();
			System.out.println("DONE");
		}
		if (this.running) {
			System.out.print("Stopping gateways... ");
			this.gatewayManager.stop();
			System.out.println("DONE");
		}
		if (this.entityManager != null) {
			System.out.print("Destroying entities... ");
			this.entityManager.destroy();
			System.out.println("DONE");
		}
		if (this.cacheManager != null) {
			System.out.print("Stopping cache manager... ");
			this.cacheManager.stop();
			System.out.println("DONE");
		}

		if (this.monitorAgentManager != null) {
			System.out.println("Stoping monitor agents...");
			this.monitorAgentManager.stop();
			System.out.println("DONE");
		}

		if (this.serviceManager != null) {
			System.out.println("Stoping service manager...");
			this.serviceManager.shutdown();
			System.out.println("DONE");
		}
	}
}
