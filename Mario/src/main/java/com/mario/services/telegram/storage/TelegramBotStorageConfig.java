package com.mario.services.telegram.storage;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TelegramBotStorageConfig {

	public static enum TelegramBotStorageType {
		FILE, MONGO, MYSQL;

		public static final TelegramBotStorageType fromName(String name) {
			if (name != null) {
				for (TelegramBotStorageType value : values()) {
					if (value.name().equalsIgnoreCase(name)) {
						return value;
					}
				}
			}
			return null;
		}
	}

	private TelegramBotStorageType type;
	private String filePath;

	private String dataSourceName;

	public static TelegramBotStorageConfig read(Node node) {
		if (node != null) {
			Node ele = node.getFirstChild();
			TelegramBotStorageConfig config = new TelegramBotStorageConfig();
			while (ele != null) {
				if (ele.getNodeType() == Element.ELEMENT_NODE) {
					String nodeName = ele.getNodeName().toLowerCase();
					String nodeValue = ele.getTextContent().trim();
					switch (nodeName) {
					case "type":
						TelegramBotStorageType type = TelegramBotStorageType.fromName(nodeValue);
						if (type == null) {
							throw new RuntimeException("Storage type not supported: " + nodeName);
						}
						config.setType(type);
						break;
					case "filepath":
						config.setFilePath(nodeValue);
						break;
					case "datasource":
						config.setDataSourceName(nodeValue);
						break;
					}
				}
				ele = ele.getNextSibling();
			}
			return config;
		}
		return null;
	}
}
