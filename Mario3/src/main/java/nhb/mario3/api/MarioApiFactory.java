package nhb.mario3.api;

import nhb.common.data.PuObjectRO;
import nhb.common.db.cassandra.CassandraDatasourceManager;
import nhb.common.db.mongodb.MongoDBSourceManager;
import nhb.common.db.sql.SQLDataSourceManager;
import nhb.mario3.cache.CacheManager;
import nhb.mario3.entity.EntityManager;
import nhb.mario3.extension.ExtensionManager;
import nhb.mario3.gateway.GatewayManager;
import nhb.mario3.monitor.MonitorAgentManager;
import nhb.mario3.producer.MessageProducerManager;
import nhb.mario3.schedule.impl.SchedulerFactory;
import nhb.mario3.zookeeper.ZooKeeperClientManager;

public final class MarioApiFactory {

	private CassandraDatasourceManager cassandraDatasourceManager;
	private SQLDataSourceManager sqlDataSourceManager;
	private SchedulerFactory schedulerFactory;
	private CacheManager cacheManager;
	private EntityManager entityManager;
	private MongoDBSourceManager mongoDBSourceManager;
	private GatewayManager gatewayManager;
	private MessageProducerManager producerManager;
	private ZooKeeperClientManager zkClientManager;

	private MonitorAgentManager monitorAgentManager;
	private ExtensionManager extensionManager;

	private PuObjectRO globalProperties;

	public MarioApiFactory(SQLDataSourceManager sqlDataSourceManager,
			CassandraDatasourceManager cassandraDatasourceManager, SchedulerFactory schedulerFactory,
			MongoDBSourceManager mongoDBSourceManager, CacheManager cacheManager,
			MonitorAgentManager monitorAgentManager, MessageProducerManager producerManager,
			GatewayManager gatewayManager, ZooKeeperClientManager zkClientManager, ExtensionManager extensionManager,
			PuObjectRO globalProperties) {
		this.cassandraDatasourceManager = cassandraDatasourceManager;
		this.sqlDataSourceManager = sqlDataSourceManager;
		this.schedulerFactory = schedulerFactory;
		this.cacheManager = cacheManager;
		this.mongoDBSourceManager = mongoDBSourceManager;
		this.monitorAgentManager = monitorAgentManager;
		this.producerManager = producerManager;
		this.gatewayManager = gatewayManager;
		this.zkClientManager = zkClientManager;
		this.extensionManager = extensionManager;

		this.globalProperties = globalProperties;
	}

	public MarioApi newApi() {
		return new MarioApiImpl(this.sqlDataSourceManager, this.cassandraDatasourceManager,
				this.schedulerFactory.newSchedulerInstance(), this.cacheManager, this.mongoDBSourceManager,
				this.entityManager, this.gatewayManager.getSocketSessionManager(), this.monitorAgentManager,
				this.producerManager, this.gatewayManager, this.zkClientManager, this.extensionManager,
				this.globalProperties.deepClone());
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setGatewayManager(GatewayManager gatewayManager) {
		this.gatewayManager = gatewayManager;
	}

}
