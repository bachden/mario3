package com.mario.api;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.hazelcast.core.HazelcastInstance;
import com.mario.cache.hazelcast.HazelcastInitializer;
import com.mario.contact.ContactBook;
import com.mario.entity.message.Message;
import com.mario.external.configuration.ExternalConfiguration;
import com.mario.gateway.Gateway;
import com.mario.gateway.serverwrapper.ServerWrapper;
import com.mario.gateway.socket.SocketSession;
import com.mario.monitor.agent.MonitorAgent;
import com.mario.schedule.Scheduler;
import com.mario.schedule.distributed.DistributedScheduler;
import com.mario.services.email.EmailService;
import com.mario.services.sms.SmsService;
import com.mario.services.telegram.TelegramBot;
import com.mongodb.MongoClient;
import com.nhb.common.cache.jedis.JedisService;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.db.cassandra.CassandraDataSource;
import com.nhb.common.db.sql.DBIAdapter;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.zmq.ZMQSocketRegistry;

@SuppressWarnings("deprecation")
public interface MarioApi {

	default String getBasePath() {
		return FileSystemUtils.getBasePath();
	}

	Scheduler getScheduler();

	DistributedScheduler getDistributedScheduler(String name);

	CassandraDataSource getCassandraDataSource(String name);

	DBIAdapter getDatabaseAdapter(String dataSourceName);

	MongoClient getMongoClient(String name);

	com.mongodb.async.client.MongoClient getAsyncMongoClient(String name);

	HazelcastInstance getHazelcastInstance(String name);

	/**
	 * this method will be removed in near future, please use "initializers" config
	 * in extension.xml instead
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

	<T extends ServerWrapper> T getServerWrapper(String name);

	ContactBook getContactBook();

	SmsService getSmsService(String name);

	EmailService getEmailService(String name);

	TelegramBot getTelegramBot(String telegramBotName);

	ExternalConfiguration getExternalConfiguration(String name);

	ZMQSocketRegistry getZMQSocketRegistry(String name);
}
