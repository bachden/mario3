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
import nhb.common.db.sql.DBIAdapter;
import nhb.common.utils.FileSystemUtils;
import nhb.mario3.cache.hazelcast.HazelcastInitializer;
import nhb.mario3.entity.message.Message;
import nhb.mario3.gateway.Gateway;
import nhb.mario3.gateway.socket.SocketSession;
import nhb.mario3.monitor.MonitorAgent;
import nhb.mario3.schedule.Scheduler;
import nhb.messaging.MessageProducer;

@SuppressWarnings("deprecation")
public interface MarioApi {

	default String getBasePath() {
		return FileSystemUtils.getBasePath();
	}

	Scheduler getScheduler();

	CassandraDataSource getCassandraDataSource(String name);

	DBIAdapter getDatabaseAdapter(String dataSourceName);

	MongoClient getMongoClient(String name);

	HazelcastInstance getHazelcastInstance(String name);

	/**
	 * this method will be removed in near future, please use "initializers"
	 * config in extension.xml instead
	 */
	HazelcastInstance getHazelcastInstance(String name, HazelcastInitializer initializer);

	JedisService getJedisService(String name);

	PuElement call(String handlerName, PuElement request);

	void request(String handlerName, Message message);

	<T> T acquireObject(String managedObjectName, PuObject requestParams);

	void releaseObject(String managedObjectName, Object objectTobeReleased);

	SocketSession getSocketSession(String sessionId);

	MonitorAgent getMonitorAgent(String name);

	ZkClient getZkClient(String name);

	ZooKeeper getZooKeeper(String name);

	ZooKeeper getZooKeeperAndAddWatcher(String name, Watcher watcher);

	<T extends MessageProducer<?>> T getProducer(String name);

	<T extends Gateway> T getGateway(String name);

	PuObjectRO getExtensionProperty(String extensionName, String propertyName);

	PuObjectRO getGlobalProperty(String name);
}
