package nhb.mario3.monitor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nhb.common.data.PuObject;
import nhb.mario3.api.MarioApiFactory;
import nhb.mario3.config.MonitorAgentConfig;

public class MonitorAgentManager {

	private Map<String, MonitorAgent> monitorAgents = new ConcurrentHashMap<>();
	private MarioApiFactory apiFactory;

	public MonitorAgentManager(MarioApiFactory apiFactory) {
		this.apiFactory = apiFactory;
	}

	public void init(Collection<MonitorAgentConfig> configs) {
		if (configs != null) {
			configs.forEach(config -> {
				try {
					MonitorAgent agent = (MonitorAgent) getClass().getClassLoader().loadClass(config.getHandleClass())
							.newInstance();
					agent.setExtensionName(config.getExtensionName());
					agent.setName(config.getName());
					agent.setApi(this.apiFactory.newApi());
					agent.setInterval(config.getInterval());
					agent.init(config.getInitParams() == null ? new PuObject() : config.getInitParams());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
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
