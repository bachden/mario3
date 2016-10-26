package com.mario.api;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.hazelcast.core.HazelcastInstance;
import com.mario.cache.hazelcast.HazelcastInitializer;
import com.mario.entity.message.Message;
import com.mario.gateway.Gateway;
import com.mario.gateway.socket.SocketSession;
import com.mario.monitor.MonitorAgent;
import com.mario.schedule.Scheduler;
import com.mongodb.MongoClient;
import com.nhb.common.cache.jedis.JedisService;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.db.cassandra.CassandraDataSource;
import com.nhb.common.db.sql.DBIAdapter;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.messaging.MessageProducer;

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
