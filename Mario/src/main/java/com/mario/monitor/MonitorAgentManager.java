package com.mario.monitor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mario.api.MarioApiFactory;
import com.mario.entity.EntityManager;
import com.mario.entity.LifeCycle;
import com.mario.monitor.config.MonitorAgentConfig;
import com.nhb.common.data.PuObject;

public class MonitorAgentManager {

	private Map<String, MonitorAgent> monitorAgents = new ConcurrentHashMap<>();

	private final MarioApiFactory apiFactory;
	private final EntityManager entityManager;

	public MonitorAgentManager(MarioApiFactory apiFactory, EntityManager entityManager) {
		this.apiFactory = apiFactory;
		this.entityManager = entityManager;
	}

	public void init(Collection<MonitorAgentConfig> configs) {
		if (configs != null) {
			configs.forEach(config -> {
				String monitorableTargetName = config.getTarget();
				LifeCycle lifeCycle = this.entityManager.getLifeCycle(monitorableTargetName);
				if (!(lifeCycle instanceof Monitorable)) {
					throw new RuntimeException("Cannot monitor a non-monitorable target named " + monitorableTargetName
							+ ", extension " + config.getExtensionName());
				}

				DefaultMonitorAgent agent = new DefaultMonitorAgent();
				agent.setName(config.getName());
				agent.setExtensionName(config.getExtensionName());
				agent.setApi(this.apiFactory.newApi());
				agent.setInterval(config.getInterval());
				agent.setAlertConfig(config.getAlertConfig());

				Monitorable monitorableObject = (Monitorable) lifeCycle;
				monitorableObject.initMonitoring(
						config.getMonitoringParams() == null ? new PuObject() : config.getMonitoringParams());

				agent.setTarget(monitorableObject);

				monitorAgents.put(agent.getName(), agent);
			});
		}
	}

	public void start() {
		for (MonitorAgent agent : this.monitorAgents.values()) {
			agent.start();
		}
	}

	public void stop() {
		for (MonitorAgent agent : this.monitorAgents.values()) {
			agent.stop();
		}
	}

	public MonitorAgent getMonitorAgent(String name) {
		return this.monitorAgents.get(name);
	}
}
