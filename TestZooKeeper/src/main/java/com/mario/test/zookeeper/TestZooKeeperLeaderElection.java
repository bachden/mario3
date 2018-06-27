package com.mario.test.zookeeper;

import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.mario.entity.impl.BaseMessageHandler;
import com.mario.schedule.ScheduledCallback;
import com.nhb.common.data.PuObjectRO;

public class TestZooKeeperLeaderElection extends BaseMessageHandler {

	private final static String ZKCLIENT = "zkclient";

	private final static String LEADER_ELECTION_ROOT_NODE = "/election";

	private static final String PROCESS_NODE_PREFIX = "/p_";

	private String zkClientName;
	private int id;

	private ZooKeeper zooKeeper;

	private String watchedNodePath;
	private String processNodePath;

	private long timeToLiveMillis;

	@Override
	public void init(PuObjectRO initParams) {
		this.id = initParams.getInteger("id");
		this.zkClientName = initParams.getString("zkclient", ZKCLIENT);
		this.timeToLiveMillis = initParams.getLong("timeToLiveMillis");
	}

	@Override
	public void destroy() throws Exception {
	}

	private void attemptForLeaderPosition() {
		try {
			final List<String> childNodePaths = zooKeeper.getChildren(LEADER_ELECTION_ROOT_NODE, false);
			Collections.sort(childNodePaths);

			getLogger().debug("[Process {}] List children: {}", this.id, childNodePaths);

			int index = childNodePaths.indexOf(processNodePath.substring(processNodePath.lastIndexOf('/') + 1));
			if (index == 0) {
				if (getLogger().isInfoEnabled()) {
					getLogger().info("[Process: " + id + "] I am the new leader!");
				}
			} else {
				final String watchedNodeShortPath = childNodePaths.get(index - 1);

				watchedNodePath = LEADER_ELECTION_ROOT_NODE + "/" + watchedNodeShortPath;

				if (getLogger().isInfoEnabled()) {
					getLogger().info("[Process: " + id + "] - Setting watch on node with path: " + watchedNodePath);
				}

				zooKeeper.exists(watchedNodePath, true);
			}
		} catch (Exception e) {
			throw new IllegalStateException("zoo keeper error");
		}
	}

	@Override
	public void onServerReady() {
		new Thread("ZooKeeper Leader Election Starter #" + this.id) {
			@Override
			public void run() {
				startElection();
			}
		}.start();
		if (this.timeToLiveMillis > 0) {
			getApi().getScheduler().schedule(this.timeToLiveMillis, new ScheduledCallback() {

				@Override
				public void call() {
					getLogger().debug("Closing zookeeper on " + id);
					try {
						zooKeeper.close();
					} catch (InterruptedException e) {
						getLogger().error("ZooKeeper cannot be closed", e);
					}
				}
			});
		}
	}

	private void startElection() {
		this.zooKeeper = getApi().getZooKeeperAndAddWatcher(zkClientName, new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				if (getLogger().isDebugEnabled()) {
					getLogger().debug("[Process: " + id + "] Event received: " + event);
				}

				final EventType eventType = event.getType();
				if (EventType.NodeDeleted.equals(eventType)) {
					if (event.getPath().equalsIgnoreCase(watchedNodePath)) {
						attemptForLeaderPosition();
					}
				}
			}
		});

		if (getLogger().isInfoEnabled()) {
			getLogger().info("Process with id: " + id + " has started!");
		}

		final String rootNodePath = createNode(LEADER_ELECTION_ROOT_NODE, false, false);
		if (rootNodePath == null) {
			throw new IllegalStateException(
					"Unable to create/access leader election root node with path: " + LEADER_ELECTION_ROOT_NODE);
		}

		processNodePath = createNode(rootNodePath + PROCESS_NODE_PREFIX, false, true);
		if (processNodePath == null) {
			throw new IllegalStateException(
					"Unable to create/access process node with path: " + LEADER_ELECTION_ROOT_NODE);
		} else {
			getLogger().info("[Process: {}] linked to znode path: {}", this.id, processNodePath);
		}

		attemptForLeaderPosition();
	}

	public String createNode(final String node, final boolean watch, final boolean ephimeral) {
		String createdNodePath = null;
		try {

			final Stat nodeStat = zooKeeper.exists(node, watch);

			if (nodeStat == null) {
				createdNodePath = zooKeeper.create(node, new byte[0], Ids.OPEN_ACL_UNSAFE,
						(ephimeral ? CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.PERSISTENT));
			} else {
				createdNodePath = node;
			}

		} catch (NodeExistsException e) {
			getLogger().debug("-----> node {} exists", node);
			createdNodePath = node;
		} catch (KeeperException | InterruptedException e) {
			throw new IllegalStateException(e);
		}

		return createdNodePath;
	}
}
