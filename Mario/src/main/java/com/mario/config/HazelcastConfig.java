package com.mario.config;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;

import lombok.Getter;
import lombok.Setter;

public class HazelcastConfig extends MarioBaseConfig {

	@Setter
	@Getter
	private boolean member;

	@Setter
	@Getter
	private String configFilePath;

	@Setter
	@Getter
	private String initializerClass;

	@Setter
	@Getter
	private boolean lazyInit = false;

	@Setter
	@Getter
	private boolean autoInitOnExtensionReady = true;

	@Getter
	private final List<String> initializers = new ArrayList<>();

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("isMember")) {
			this.setMember(data.getBoolean("isMember"));
		} else if (data.variableExists("member")) {
			this.setMember(data.getBoolean("member"));
		}

		if (data.variableExists("configFilePath")) {
			this.setConfigFilePath(data.getString("configFilePath"));
		} else if (data.variableExists("configFile")) {
			this.setConfigFilePath(data.getString("configFile"));
		} else if (data.variableExists("config")) {
			this.setConfigFilePath(data.getString("config"));
		}

		if (data.variableExists("lazyinit")) {
			this.setLazyInit(data.getBoolean("lazyinit"));
		}

		if (data.variableExists("autoInit")) {
			this.setLazyInit(data.getBoolean("autoInit"));
		} else if (data.variableExists("autoInitOnExtensionReady")) {
			this.setLazyInit(data.getBoolean("autoInitOnExtensionReady"));
		}

		if (data.variableExists("initializers")) {
			for (PuValue entry : data.getPuArray("initializers")) {
				this.initializers.add(entry.getString());
			}
		}
	}

	@Override
	public void readNode(Node item) {
		Node curr = item.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == 1) {
				String value = curr.getTextContent().trim();
				switch (curr.getNodeName().trim().toLowerCase()) {
				case "name":
					this.setName(value);
					break;
				case "config":
				case "configfile":
					this.setConfigFilePath(value);
					break;
				case "member":
				case "ismember":
					this.setMember(Boolean.valueOf(value));
					break;
				case "initializer":
				case "initializerClass":
					getLogger().warn(
							"the initializer class config is now DEPRECATED, please use 'initializers' to specific lifecycle names");
					this.setInitializerClass(value);
					break;
				case "lazyinit":
				case "islazyinit":
					this.setLazyInit(Boolean.valueOf(value));
					break;
				case "autoinit":
				case "autoinitonextensionready":
					this.setAutoInitOnExtensionReady(Boolean.valueOf(value));
					break;
				case "initializers":
					Node entryNode = curr.getFirstChild();
					while (entryNode != null) {
						if (entryNode.getNodeType() == Node.ELEMENT_NODE) {
							this.getInitializers().add(entryNode.getTextContent().trim());
						}
						entryNode = entryNode.getNextSibling();
					}
					break;
				default:
					break;
				}
			}
			curr = curr.getNextSibling();
		}
	}
}
