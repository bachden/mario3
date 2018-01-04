package com.mario.api;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.hazelcast.core.HazelcastInstance;
import com.mario.cache.CacheManager;
import com.mario.cache.hazelcast.HazelcastInitializer;
import com.mario.contact.ContactBook;
import com.mario.entity.EntityManager;
import com.mario.entity.ManagedObject;
import com.mario.entity.MessageHandler;
import com.mario.entity.message.DecodingErrorMessage;
import com.mario.entity.message.Message;
import com.mario.entity.message.MessageRW;
import com.mario.exceptions.ManagedObjectNotFoundException;
import com.mario.exceptions.MessageHandlerNotFoundException;
import com.mario.extension.ExtensionManager;
import com.mario.external.configuration.ExternalConfiguration;
import com.mario.external.configuration.ExternalConfigurationManager;
import com.mario.gateway.Gateway;
import com.mario.gateway.GatewayManager;
import com.mario.gateway.serverwrapper.ServerWrapper;
import com.mario.gateway.serverwrapper.ServerWrapperManager;
import com.mario.gateway.socket.SocketSession;
import com.mario.gateway.socket.SocketSessionManager;
import com.mario.monitor.MonitorAgentManager;
import com.mario.monitor.agent.MonitorAgent;
import com.mario.producer.MessageProducerManager;
import com.mario.schedule.Scheduler;
import com.mario.schedule.distributed.DistributedScheduler;
import com.mario.schedule.distributed.impl.HzDistributedSchedulerManager;
import com.mario.services.ServiceManager;
import com.mario.services.email.EmailService;
import com.mario.services.sms.SmsService;
import com.mario.services.telegram.TelegramBot;
import com.mario.zeromq.ZMQSocketRegistryManager;
import com.mario.zookeeper.ZooKeeperClientManager;
import com.mongodb.MongoClient;
import com.nhb.common.cache.jedis.JedisService;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.db.cassandra.CassandraDataSource;
import com.nhb.common.db.cassandra.CassandraDatasourceManager;
import com.nhb.common.db.mongodb.MongoDBSourceManager;
import com.nhb.common.db.sql.DBIAdapter;
import com.nhb.common.db.sql.SQLDataSourceManager;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.zmq.ZMQSocketRegistry;

import lombok.Getter;

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
	private ExternalConfigurationManager externalConfigurationManager;
	private ZMQSocketRegistryManager zmqSocketRegistryMamager;

	@Getter
	private ContactBook contactBook;

	private PuObjectRO globalProperties;
	private ServerWrapperManager serverWrapperManager;
	private ServiceManager serviceManager;
	private HzDistributedSchedulerManager hzDistributedSchedulerManager;

	MarioApiImpl(SQLDataSourceManager dataSourceManager, CassandraDatasourceManager cassandraDatasourceManager,
			Scheduler scheduler, CacheManager cacheManager, MongoDBSourceManager mongoDBSourceManager,
			EntityManager entityManager, SocketSessionManager sessionManager, MonitorAgentManager monitorAgentManager,
			MessageProducerManager producerManager, GatewayManager gatewayManager,
			ZooKeeperClientManager zkClientManager, ExtensionManager extensionManager,
			ServerWrapperManager serverWrapperManager, PuObjectRO globalProperties, ContactBook contactBook,
			ServiceManager serviceManager, HzDistributedSchedulerManager hzDistributedSchedulerManager,
			ExternalConfigurationManager externalConfigurationManager,
			ZMQSocketRegistryManager zmqSocketRegistryMamager) {

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
		this.serverWrapperManager = serverWrapperManager;

		this.globalProperties = globalProperties;

		this.contactBook = contactBook;
		this.serviceManager = serviceManager;
		this.hzDistributedSchedulerManager = hzDistributedSchedulerManager;
		this.externalConfigurationManager = externalConfigurationManager;
		this.zmqSocketRegistryMamager = zmqSocketRegistryMamager;
	}

	@Override
	public ZMQSocketRegistry getZMQSocketRegistry(String name) {
		return this.zmqSocketRegistryMamager.getZMQSocketRegistry(name);
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

			try {
				PuElement result = null;
				boolean hasError = false;
				if (message instanceof DecodingErrorMessage
						&& ((DecodingErrorMessage) message).getDecodingFailedCause() != null) {
					if (message.getCallback() != null) {
						message.getCallback().onHandleError(message,
								((DecodingErrorMessage) message).getDecodingFailedCause());
					}
					hasError = true;
				} else {
					try {
						result = handler.handle(message);
					} catch (Exception e) {
						if (message.getCallback() != null) {
							message.getCallback().onHandleError(message, e);
						}
						hasError = true;
					}
				}
				if (!hasError && message.getCallback() != null) {
					message.getCallback().onHandleComplete(message, result);
				}
			} finally {
				if (message instanceof MessageRW) {
					((MessageRW) message).clear();
				}
			}
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
	public com.mongodb.async.client.MongoClient getAsyncMongoClient(String name) {
		assert name != null;
		assert this.mongoDBSourceManager != null;
		return this.mongoDBSourceManager.getAsyncMongoClient(name);
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
	 * this method will be removed in near future, please use "initializers" config
	 * in extension.xml
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
	public <T extends ServerWrapper> T getServerWrapper(String name) {
		return this.serverWrapperManager.getServerWrapper(name);
	}

	@Override
	public PuObjectRO getGlobalProperty(String name) {
		return this.globalProperties.getPuObject(name);
	}

	@Override
	public <R> SmsService<R> getSmsService(String name) {
		return this.serviceManager.getSmsService(name);
	}

	@Override
	public EmailService getEmailService(String name) {
		return this.serviceManager.getEmailService(name);
	}

	@Override
	public TelegramBot getTelegramBot(String telegramBotName) {
		return this.serviceManager.getTelegramBot(telegramBotName);
	}

	@Override
	public DistributedScheduler getDistributedScheduler(String name) {
		return this.hzDistributedSchedulerManager.getDistributedScheduler(name);
	}

	@Override
	public ExternalConfiguration getExternalConfiguration(String name) {
		return this.externalConfigurationManager.getConfig(name);
	}
}
