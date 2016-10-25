package nhb.mario3.api;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.hazelcast.core.HazelcastInstance;
import com.mongodb.MongoClient;

import nhb.common.cache.jedis.JedisService;
import nhb.common.data.PuElement;
import nhb.common.data.PuObject;
import nhb.common.data.PuObjectRO;
import nhb.common.db.cassandra.CassandraDataSource;
import nhb.common.db.cassandra.CassandraDatasourceManager;
import nhb.common.db.mongodb.MongoDBSourceManager;
import nhb.common.db.sql.DBIAdapter;
import nhb.common.db.sql.SQLDataSourceManager;
import nhb.mario3.cache.CacheManager;
import nhb.mario3.cache.hazelcast.HazelcastInitializer;
import nhb.mario3.entity.EntityManager;
import nhb.mario3.entity.ManagedObject;
import nhb.mario3.entity.MessageHandler;
import nhb.mario3.entity.message.Message;
import nhb.mario3.exceptions.ManagedObjectNotFoundException;
import nhb.mario3.exceptions.MessageHandlerNotFoundException;
import nhb.mario3.extension.ExtensionManager;
import nhb.mario3.gateway.Gateway;
import nhb.mario3.gateway.GatewayManager;
import nhb.mario3.gateway.socket.SocketSession;
import nhb.mario3.gateway.socket.SocketSessionManager;
import nhb.mario3.monitor.MonitorAgent;
import nhb.mario3.monitor.MonitorAgentManager;
import nhb.mario3.producer.MessageProducerManager;
import nhb.mario3.schedule.Scheduler;
import nhb.mario3.zookeeper.ZooKeeperClientManager;
import nhb.messaging.MessageProducer;

@SuppressWarnings("deprecation")
class MarioApiImpl implements MarioApi {

	private CassandraDatasourceManager cassandraDatasourceManager;
	private SQLDataSourceManager sqlDatasourceManager;
	private MongoDBSourceManager mongoDBSourceManager;
	private Scheduler scheduler;
	private CacheManager cacheManager;
	private EntityManager entityManager;
	private SocketSessionManager sessionManager;
	private MonitorAgentManager monitorAgentManager;
	private MessageProducerManager producerManager;
	private GatewayManager gatewayManager;
	private ZooKeeperClientManager zkClientManager;
	private ExtensionManager extensionManager;

	private PuObjectRO globalProperties;

	MarioApiImpl(SQLDataSourceManager dataSourceManager, CassandraDatasourceManager cassandraDatasourceManager,
			Scheduler scheduler, CacheManager cacheManager, MongoDBSourceManager mongoDBSourceManager,
			EntityManager entityManager, SocketSessionManager sessionManager, MonitorAgentManager monitorAgentManager,
			MessageProducerManager producerManager, GatewayManager gatewayManager,
			ZooKeeperClientManager zkClientManager, ExtensionManager extensionManager, PuObjectRO globalProperties) {

		this.sqlDatasourceManager = dataSourceManager;
		this.cassandraDatasourceManager = cassandraDatasourceManager;
		this.scheduler = scheduler;
		this.cacheManager = cacheManager;
		this.entityManager = entityManager;
		this.mongoDBSourceManager = mongoDBSourceManager;
		this.sessionManager = sessionManager;
		this.monitorAgentManager = monitorAgentManager;
		this.producerManager = producerManager;
		this.gatewayManager = gatewayManager;
		this.zkClientManager = zkClientManager;
		this.extensionManager = extensionManager;

		this.globalProperties = globalProperties;
	}

	@Override
	public DBIAdapter getDatabaseAdapter(String dataSourceName) {
		DBIAdapter result = new DBIAdapter(sqlDatasourceManager, dataSourceName);
		return result;
	}

	@Override
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	@Override
	public HazelcastInstance getHazelcastInstance(String name) {
		return this.cacheManager.getHazelcastInstance(name);
	}

	@Override
	public PuElement call(String handlerName, PuElement request) {
		MessageHandler handler = this.entityManager.getMessageHandler(handlerName);
		if (handler != null) {
			return handler.interop(request);
		} else {
			throw new MessageHandlerNotFoundException("Message handler not found for name: " + handlerName);
		}
	}

	@Override
	public void request(String handlerName, Message message) {
		MessageHandler handler = this.entityManager.getMessageHandler(handlerName);
		if (handler != null) {
			handler.handle(message);
		} else {
			throw new MessageHandlerNotFoundException("Message handler not found for name: " + handlerName);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T acquireObject(String managedObjectName, PuObject requestParams) {
		if (managedObjectName != null) {
			ManagedObject managedObject = this.entityManager.getManagedObject(managedObjectName);
			if (managedObject != null) {
				Object result = managedObject.acquire(requestParams);
				if (result != null) {
					return (T) result;
				}
			} else {
				throw new ManagedObjectNotFoundException("ManagedObject not found for name: " + managedObjectName);
			}
		}
		return null;
	}

	@Override
	public void releaseObject(String managedObjectName, Object objectTobeReleased) {
		if (managedObjectName != null) {
			ManagedObject managedObject = this.entityManager.getManagedObject(managedObjectName);
			if (managedObject != null) {
				managedObject.release(objectTobeReleased);
			} else {
				throw new ManagedObjectNotFoundException("ManagedObject not found for name: " + managedObjectName);
			}
		}
	}

	@Override
	public MongoClient getMongoClient(String name) {
		assert name != null;
		assert this.mongoDBSourceManager != null;
		return this.mongoDBSourceManager.getMongoClient(name);
	}

	@Override
	public SocketSession getSocketSession(String sessionId) {
		return this.sessionManager.getSessionFromId(sessionId);
	}

	@Override
	public JedisService getJedisService(String name) {
		return this.cacheManager.getJedisServiceByName(name);
	}

	@Override
	public MonitorAgent getMonitorAgent(String name) {
		return this.monitorAgentManager == null ? null : this.monitorAgentManager.getMonitorAgent(name);
	}

	@Override
	public <T extends MessageProducer<?>> T getProducer(String name) {
		return this.producerManager.getProducer(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Gateway> T getGateway(String name) {
		return (T) this.gatewayManager.getGatewayByName(name);
	}

	@Override
	public CassandraDataSource getCassandraDataSource(String name) {
		return this.cassandraDatasourceManager == null ? null : this.cassandraDatasourceManager.getDatasource(name);
	}

	/**
	 * this method will be removed in near future, please use "initializers"
	 * config in extension.xml
	 */
	@Override
	public HazelcastInstance getHazelcastInstance(String name, HazelcastInitializer initializer) {
		return this.cacheManager.getHazelcastInstance(name, initializer);
	}

	@Override
	public ZkClient getZkClient(String name) {
		return zkClientManager.acquireZkClient(name);
	}

	@Override
	public ZooKeeper getZooKeeper(String name) {
		return zkClientManager.acquireZooKeeper(name);
	}

	@Override
	public ZooKeeper getZooKeeperAndAddWatcher(String name, Watcher watcher) {
		ZooKeeper result = this.getZooKeeper(name);
		zkClientManager.addWatcher(name, watcher);
		return result;
	}

	@Override
	public PuObjectRO getExtensionProperty(String extensionName, String propertyName) {
		return this.extensionManager.getExtensionProperty(extensionName, propertyName);
	}

	@Override
	public PuObjectRO getGlobalProperty(String name) {
		return this.globalProperties.getPuObject(name);
	}
}
