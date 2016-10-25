package nhb.mario3.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nhb.eventdriven.impl.BaseEvent;
import nhb.eventdriven.impl.BaseEventDispatcher;
import nhb.mario3.api.MarioApiFactory;
import nhb.mario3.config.LifeCycleConfig;
import nhb.mario3.extension.ExtensionManager;

public final class EntityManager extends BaseEventDispatcher {

	private ExtensionManager extensionManager;

	private Map<String, LifeCycle> lifeCyclesByName;
	private List<ManagedObject> managedObjects = new ArrayList<ManagedObject>();
	private List<MessageHandler> messageHandlers = new ArrayList<MessageHandler>();
	private Map<String, LifeCycleConfig> lifeCycleNameToConfigMapping = new HashMap<String, LifeCycleConfig>();
	private MarioApiFactory apiFactory;

	public EntityManager(ExtensionManager extMan, MarioApiFactory apiFactory) {
		this.extensionManager = extMan;
		this.apiFactory = apiFactory;
		this.apiFactory.setEntityManager(this);
	}

	public void init() throws Exception {
		if (this.extensionManager.isLoaded()) {
			this.lifeCyclesByName = new ConcurrentHashMap<>();
			List<LifeCycleConfig> configs = this.extensionManager.getLifeCycleConfigs();
			if (configs != null && configs.size() > 0) {
				for (LifeCycleConfig config : configs) {
					LifeCycle lifeCycle = extensionManager.newInstance(config.getExtensionName(),
							config.getHandleClass());
					if (lifeCycle != null) {
						lifeCycle.setName(config.getName());
						lifeCycle.setExtensionName(config.getExtensionName());
						this.lifeCyclesByName.put(lifeCycle.getName(), lifeCycle);
						if (lifeCycle instanceof ManagedObject) {
							managedObjects.add((ManagedObject) lifeCycle);
						} else if (lifeCycle instanceof MessageHandler) {
							messageHandlers.add((MessageHandler) lifeCycle);
						}
						lifeCycleNameToConfigMapping.put(lifeCycle.getName(), config);
					} else {
						getLogger().error(
								"cannot create lifeCycle instance named: " + config.getName() + " with handle class: "
										+ config.getHandleClass() + ", extension name: " + config.getExtensionName());
					}
				}
				if (this.lifeCyclesByName.size() > 0) {
					this.lifeCyclesByName.values().forEach(lifeCycle -> {
						if (lifeCycle instanceof Pluggable) {
							((Pluggable) lifeCycle).setApi(this.apiFactory.newApi());
						}
					});
					if (managedObjects.size() > 0) {
						managedObjects.forEach(managedObject -> {
							managedObject
									.init(lifeCycleNameToConfigMapping.get(managedObject.getName()).getInitParams());
						});
					}
					this.lifeCyclesByName.forEach((name, lifeCycle) -> {
						if (!managedObjects.contains(lifeCycle)) {
							lifeCycle.init(lifeCycleNameToConfigMapping.get(name).getInitParams());
						}
					});

					try {
						this.dispatchEvent(new BaseEvent("initComplete"));
					} catch (Exception e) {
						getLogger().error("Error while handling initComplete event from entity manager", e);
					}

					this.managedObjects.forEach((entry) -> {
						try {
							entry.onExtensionReady();
						} catch (Exception ex) {
							getLogger().error("Error while notify extension ready to entity: " + entry.getName()
									+ ", extensionName: " + entry.getExtensionName(), ex);
						}
					});

					this.lifeCyclesByName.forEach((name, entry) -> {
						if (!this.messageHandlers.contains(entry) && !this.managedObjects.contains(entry)) {
							try {
								entry.onExtensionReady();
							} catch (Exception ex) {
								getLogger().error("Error while notify extension ready to entity: " + entry.getName()
										+ ", extensionName: " + entry.getExtensionName(), ex);
							}
						}
					});

					this.messageHandlers.forEach((entry) -> {
						try {
							entry.onExtensionReady();
						} catch (Exception ex) {
							getLogger().error("Error while notify extension ready to entity: " + entry.getName()
									+ ", extensionName: " + entry.getExtensionName(), ex);
						}
					});
				}
			}
		} else {
			throw new IllegalAccessException("cannot access extension manager while it hasn't been loaded");
		}
	}

	public void destroy() {
		if (this.lifeCyclesByName != null) {
			for (LifeCycle entry : this.lifeCyclesByName.values()) {
				try {
					entry.destroy();
				} catch (Exception ex) {
					System.err.println("destroy life cycle error: ");
					ex.printStackTrace();
				}
			}
		}
	}

	public LifeCycle getLifeCycle(String name) {
		if (name != null) {
			return this.lifeCyclesByName.get(name.trim());
		}
		return null;
	}

	public MessageHandler getMessageHandler(String handlerName) {
		LifeCycle result = this.getLifeCycle(handlerName);
		if (result instanceof MessageHandler) {
			return (MessageHandler) result;
		}
		return null;
	}

	public ManagedObject getManagedObject(String handlerName) {
		LifeCycle result = this.getLifeCycle(handlerName);
		if (result instanceof ManagedObject) {
			return (ManagedObject) result;
		}
		return null;
	}

	public List<ManagedObject> getManagedObjects() {
		return managedObjects;
	}

	public List<MessageHandler> getMessageHandlers() {
		return messageHandlers;
	}

	@SuppressWarnings("unchecked")
	public <T extends LifeCycleConfig> T getConfig(String lifeCycleName) {
		return (T) this.lifeCycleNameToConfigMapping.get(lifeCycleName);
	}

	public void dispatchServerReady() {
		for (LifeCycle entity : this.lifeCyclesByName.values()) {
			try {
				entity.onServerReady();
			} catch (Exception ex) {
				getLogger().error("Error while notify server ready to entity: " + entity.getName() + ", extension: "
						+ entity.getExtensionName(), ex);
			}
		}
	}
}
