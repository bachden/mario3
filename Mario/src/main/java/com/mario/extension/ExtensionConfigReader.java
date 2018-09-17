package com.mario.extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mario.config.CassandraConfig;
import com.mario.config.ExternalConfigurationConfig;
import com.mario.config.ExternalConfigurationConfig.ExternalConfigurationParserConfig;
import com.mario.config.HazelcastConfig;
import com.mario.config.HttpMessageProducerConfig;
import com.mario.config.KafkaMessageProducerConfig;
import com.mario.config.LifeCycleConfig;
import com.mario.config.ManagedObjectConfig;
import com.mario.config.MessageHandlerConfig;
import com.mario.config.MessageProducerConfig;
import com.mario.config.RabbitMQProducerConfig;
import com.mario.config.RedisConfig;
import com.mario.config.SSLContextConfig;
import com.mario.config.ZMQSocketRegistryConfig;
import com.mario.config.ZeroMQProducerConfig;
import com.mario.config.ZkClientConfig;
import com.mario.config.gateway.GatewayConfig;
import com.mario.config.gateway.GatewayType;
import com.mario.config.gateway.HttpGatewayConfig;
import com.mario.config.gateway.KafkaGatewayConfig;
import com.mario.config.gateway.RabbitMQGatewayConfig;
import com.mario.config.gateway.SocketGatewayConfig;
import com.mario.config.gateway.ZeroMQGatewayConfig;
import com.mario.config.serverwrapper.HttpServerWrapperConfig;
import com.mario.config.serverwrapper.RabbitMQServerWrapperConfig;
import com.mario.config.serverwrapper.ServerWrapperConfig;
import com.mario.config.serverwrapper.ServerWrapperConfig.ServerWrapperType;
import com.mario.contact.ContactBook;
import com.mario.extension.xml.EndpointReader;
import com.mario.external.configuration.ExternalConfigurationManager;
import com.mario.monitor.config.MonitorAgentConfig;
import com.mario.schedule.distributed.impl.config.HzDistributedSchedulerConfigManager;
import com.mario.services.ServiceManager;
import com.mario.zeromq.ZMQSocketRegistryManager;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.exception.InvalidDataException;
import com.nhb.common.db.mongodb.config.MongoDBConfig;
import com.nhb.common.db.mongodb.config.MongoDBCredentialConfig;
import com.nhb.common.db.mongodb.config.MongoDBReadPreferenceConfig;
import com.nhb.common.db.sql.SQLDataSourceConfig;
import com.nhb.common.exception.UnsupportedTypeException;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.common.vo.HostAndPort;

import lombok.Getter;

class ExtensionConfigReader extends XmlConfigReader {

	@Getter
	private String extensionName;

	@Getter
	private final List<LifeCycleConfig> lifeCycleConfigs = new ArrayList<>();
	@Getter
	private final List<GatewayConfig> gatewayConfigs = new ArrayList<>();
	@Getter
	private final List<SQLDataSourceConfig> sqlDatasourceConfigs = new ArrayList<>();
	@Getter
	private final List<HazelcastConfig> hazelcastConfigs = new ArrayList<>();
	@Getter
	private final List<RedisConfig> redisConfigs = new ArrayList<>();
	@Getter
	private final List<MongoDBConfig> mongoDBConfigs = new ArrayList<>();
	@Getter
	private final List<ServerWrapperConfig> serverWrapperConfigs = new ArrayList<>();
	@Getter
	private final List<MonitorAgentConfig> monitorAgentConfigs = new ArrayList<>();
	@Getter
	private final List<MessageProducerConfig> producerConfigs = new ArrayList<>();
	@Getter
	private final List<SSLContextConfig> sslContextConfigs = new ArrayList<>();
	@Getter
	private final Map<String, PuObjectRO> properties = new HashMap<>();
	@Getter
	private final List<ZkClientConfig> zkClientConfigs = new ArrayList<>();

	@Getter
	private final Collection<CassandraConfig> cassandraConfigs = new HashSet<>();

	private final PuObjectRO globalProperties;

	private final ContactBook contactBook;

	private final ServiceManager serviceManager;

	private final HzDistributedSchedulerConfigManager hzDistributedSchedulerConfigManager;

	private final ExternalConfigurationManager externalConfigurationManager;

	private final ZMQSocketRegistryManager zmqSocketRegistryManager;

	public ExtensionConfigReader(PuObjectRO globalProperties, ContactBook contactBook, ServiceManager serviceManager,
			ExternalConfigurationManager externalConfigurationManager,
			HzDistributedSchedulerConfigManager hzDistributedSchedulerConfigManager,
			ZMQSocketRegistryManager zmqSocketRegistryManager) {
		this.globalProperties = globalProperties;
		this.contactBook = contactBook;
		this.serviceManager = serviceManager;
		this.hzDistributedSchedulerConfigManager = hzDistributedSchedulerConfigManager;
		this.externalConfigurationManager = externalConfigurationManager;
		this.zmqSocketRegistryManager = zmqSocketRegistryManager;
	}

	@Override
	protected void read(Document document) throws Exception {
		System.out.println("\t\t\t- Reading extension name");
		this.extensionName = ((Node) xPath.compile("/mario/name").evaluate(document, XPathConstants.NODE))
				.getTextContent();

		if (extensionName == null || extensionName.trim().length() == 0) {
			throw new RuntimeException("extension cannot be empty");
		}

		try {
			System.out.println("\t\t\t- Reading properties");
			this.readProperties((Node) xPath.compile("/mario/properties").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading external config");
			this.readExternal((Node) xPath.compile("/mario/external").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading contacts");
			this.readContacts((Node) xPath.compile("/mario/contacts").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading services");
			this.readServices((Node) xPath.compile("/mario/services").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading scheduler configs");
			this.readDistributedSchedulerConfigs(
					(Node) xPath.compile("/mario/schedulers").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading SSL Context Config");
			this.readSSLContextConfigs((Node) xPath.compile("/mario/ssl").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading datasources config");
			this.readDataSourceConfigs(
					(Node) xPath.compile("/mario/datasources").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading zeromq configs");
			this.readZeroMQConfigs((Node) xPath.compile("/mario/zeromq").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading server wrapper config");
			this.readServerWrapperConfigs(
					(Node) xPath.compile("/mario/servers").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading gateway configs");
			this.readGatewayConfigs((Node) xPath.compile("/mario/gateways").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading lifecycle configs");
			this.readLifeCycleConfigs(
					(Node) xPath.compile("/mario/lifecycles").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading monitor agent configs");
			this.readMonitorAgentConfigs(
					(Node) xPath.compile("/mario/monitor").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading producer configs");
			this.readProducerConfigs((Node) xPath.compile("/mario/producers").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		try {
			System.out.println("\t\t\t- Reading zookeeper configs");
			this.readCooperationConfigs(
					(Node) xPath.compile("/mario/cooperations").evaluate(document, XPathConstants.NODE));
			this.readCooperationConfigs(
					(Node) xPath.compile("/mario/cooperation").evaluate(document, XPathConstants.NODE));
		} catch (Exception ex) {
			if (!(ex instanceof TransformerException) && !(ex instanceof XPathExpressionException)) {
				getLogger().error("Error", ex);
			}
		}

		System.out.println("\t\t\t- *** Reading configs done ***");
	}

	private void readZeroMQConfigs(Node root) {
		if (root != null) {
			Node curr = root.getFirstChild();
			while (curr != null) {
				if (curr.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = curr.getNodeName();
					if (nodeName.equalsIgnoreCase("registry")) {
						ZMQSocketRegistryConfig config = new ZMQSocketRegistryConfig();
						Node childNode = curr.getFirstChild();
						while (childNode != null) {
							String text = childNode.getTextContent().trim();
							switch (childNode.getNodeName().toLowerCase()) {
							case "name":
								config.setName(text);
								break;
							case "numiothreads":
								config.setNumIOThreads(Integer.valueOf(text));
								break;
							}
							childNode = childNode.getNextSibling();
						}
						config.setExtensionName(getExtensionName());
						this.zmqSocketRegistryManager.addConfig(config);
					}
				}
				curr = curr.getNextSibling();
			}
		}
	}

	private ExternalConfigurationConfig readExternalConfigurationConfig(Node root) {
		if (root != null) {
			ExternalConfigurationConfig result = new ExternalConfigurationConfig();
			Node curr = root.getFirstChild();
			while (curr != null) {
				if (curr.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = curr.getNodeName();
					String nodeValue = curr.getTextContent().trim();
					switch (nodeName.toLowerCase()) {
					case "name":
						result.setName(nodeValue);
						break;
					case "file":
					case "path":
						result.setFilePath(nodeValue);
						break;
					case "monitored":
						result.setMonitored(Boolean.valueOf(nodeValue));
						break;
					case "sensitivity":
						result.setSensitivity(nodeValue);
						break;
					case "parser":
						Node _curr = curr.getFirstChild();
						ExternalConfigurationParserConfig parserConfig = new ExternalConfigurationParserConfig();
						while (_curr != null) {
							if (_curr.getNodeType() == Node.ELEMENT_NODE) {
								String _nodeName = _curr.getNodeName();
								switch (_nodeName.toLowerCase()) {
								case "handler":
									parserConfig.setHandler(_curr.getTextContent().trim());
									break;
								case "variables":
									parserConfig.setInitParams(PuObject.fromXML(_curr));
									break;
								}
							}
							_curr = _curr.getNextSibling();
						}
						result.setParserConfig(parserConfig);
						break;
					}
				}
				curr = curr.getNextSibling();
			}
			result.setExtensionName(this.getExtensionName());
			return result;
		}
		return null;
	}

	private void readExternal(Node root) {
		if (root != null) {
			Node curr = root.getFirstChild();
			while (curr != null) {
				if (curr.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = curr.getNodeName().trim();
					switch (nodeName.toLowerCase()) {
					case "configuration":
						this.externalConfigurationManager.add(this.readExternalConfigurationConfig(curr));
						break;
					}
				}
				curr = curr.getNextSibling();
			}
		}
	}

	private void readDistributedSchedulerConfigs(Node node) {
		if (node == null) {
			return;
		}
		this.hzDistributedSchedulerConfigManager.read(node);
	}

	private void readServices(Node node) {
		if (node == null) {
			return;
		}
		this.serviceManager.readFromXml(node, extensionName);
	}

	private void readContacts(Node node) {
		if (node == null) {
			return;
		}
		this.contactBook.readFromXml(node);
	}

	private SSLContextConfig _readSSLContextConfig(Node node) {
		if (node != null) {
			Node curr = node.getFirstChild();
			SSLContextConfig result = new SSLContextConfig();
			result.setExtensionName(extensionName);
			while (curr != null) {
				if (curr.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = curr.getNodeName();
					String nodeValue = curr.getTextContent().trim();
					switch (nodeName.toLowerCase()) {
					case "name":
						result.setName(nodeValue);
						break;
					case "format":
						result.setFormat(nodeValue);
						break;
					case "protocol":
						result.setProtocol(nodeValue);
						break;
					case "algorithm":
						result.setAlgorithm(nodeValue);
						break;
					case "filepath":
						result.setFilePath(nodeValue);
						break;
					case "password":
						result.setPassword(nodeValue);
						break;
					}
				}
				curr = curr.getNextSibling();
			}
			return result;
		}
		return null;
	}

	private void readSSLContextConfigs(Node evaluate) {
		if (evaluate == null) {
			return;
		}
		Node curr = evaluate.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Node.ELEMENT_NODE) {
				if (curr.getNodeName().equals("context")) {
					sslContextConfigs.add(this._readSSLContextConfig(curr));
				} else {
					throw new Error("ssl context config must under path /mario/ssl/context");
				}
			}
			curr = curr.getNextSibling();
		}
	}

	private String extractNodeContent(Node node) {
		switch (node.getNodeType()) {
		case Node.ATTRIBUTE_NODE:
			return node.getNodeValue();
		case Node.ELEMENT_NODE:
			return node.getTextContent().trim();
		default:
			getLogger().warn("Invalid node type for read content, extension: {}", this.getExtensionName());
			break;
		}
		return null;
	}

	private InputStream readFile(final String inputFile) throws FileNotFoundException {
		String filePath = inputFile;
		if (!(filePath.startsWith(File.separator) || filePath.charAt(1) == ':')) {
			filePath = FileSystemUtils.createAbsolutePathFrom(System.getProperty("application.extensionsFolder"),
					this.getExtensionName(), filePath);
		}
		return new FileInputStream(filePath);
	}

	private void readProperties(Node properties) {
		if (properties == null) {
			return;
		}
		Node entry = properties.getFirstChild();
		while (entry != null) {
			if (entry.getNodeType() == Node.ELEMENT_NODE) {
				if (entry.getNodeName().equalsIgnoreCase("entry")) {

					Node nameAttr = entry.getAttributes().getNamedItem("name");
					Node refAttr = entry.getAttributes().getNamedItem("ref");
					Node fileAttr = entry.getAttributes().getNamedItem("file");

					String name = nameAttr == null ? null : nameAttr.getNodeValue().trim();
					String ref = refAttr == null ? null : refAttr.getNodeValue().trim();
					String file = fileAttr == null ? null : fileAttr.getNodeValue().trim();
					PuObject contentData = null;

					Node node = entry.getFirstChild();
					while (node != null) {
						if (node.getNodeType() == Node.ELEMENT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE) {
							String nodeName = node.getNodeName().toLowerCase();
							switch (nodeName) {
							case "name":
								name = extractNodeContent(node);
								break;
							case "ref":
								ref = extractNodeContent(node);
								break;
							case "file":
								file = extractNodeContent(node);
								break;
							case "variables":
								if (node.getNodeType() != Node.ELEMENT_NODE) {
									getLogger().warn("Variables node must be ELEMENT_NODE, extension: "
											+ this.getExtensionName());
								} else {
									contentData = PuObject.fromXML(node);
								}
								break;
							default:
								getLogger().warn("Unrecoginzed property's config name: " + nodeName + ", extension: "
										+ this.getExtensionName());
								break;
							}
						}
						node = node.getNextSibling();
					}

					PuObjectRO fileData = null;
					PuObjectRO refData = null;

					if (name == null) {
						throw new NullPointerException(
								"Property name cannot be null, extension: " + this.getExtensionName());
					}

					if (ref != null) {
						refData = this.getRefProperty(ref);
					}

					if (file != null) {
						try (InputStream is = readFile(file); StringWriter sw = new StringWriter()) {
							IOUtils.copy(is, sw);
							fileData = PuObject.fromXML(sw.toString());
						} catch (Exception e) {
							throw new RuntimeException("Exception while loading property from file, extension: "
									+ this.getExtensionName() + ", filePath: " + file);
						}
					}

					if (refData == null && fileData == null && contentData == null) {
						throw new InvalidDataException(
								"All 3 file data (config by 'file' attribute), content data (config by 'variables' tag) and refData are null");
					}

					PuObject data = new PuObject();
					data.addAll(refData);
					data.addAll(fileData);
					data.addAll(contentData);

					this.properties.put(name, data);
				} else {
					getLogger().warn("Invalid node name, expected for 'entry', extension: " + this.extensionName);
				}
			} else {
				getLogger().warn("Invalid node type, expected for ELEMENT_NODE, extension: " + this.extensionName);
			}
			entry = entry.getNextSibling();
		}
	}

	private ZkClientConfig readZkClientConfig(Node node) {
		if (node == null) {
			throw new NullPointerException("ZkClientConfig xml node cannot be null");
		}
		ZkClientConfig config = new ZkClientConfig();
		Node refAttr = node.getAttributes().getNamedItem("ref");
		if (refAttr != null) {
			String ref = refAttr.getNodeValue();
			PuObjectRO refObj = this.getRefProperty(ref);
			if (ref != null) {
				config.readPuObject(refObj);
			}
		}
		Node ele = node.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = ele.getNodeName();
				String nodeValue = ele.getTextContent().trim();
				switch (nodeName.toLowerCase()) {
				case "name":
					config.setName(nodeValue);
					break;
				case "servers":
					config.setServers(nodeValue);
					break;
				case "sessiontimeout":
					config.setSessionTimeout(Integer.valueOf(nodeValue));
					break;
				case "connectiontimeout":
					config.setConnectionTimeout(Integer.valueOf(nodeValue));
					break;
				case "serializerclass":
					config.setSerializerClass(nodeValue);
					break;
				case "operationretrytimeout":
					config.setOperationRetryTimeout(Long.valueOf(nodeValue));
					break;
				default:
					getLogger().warn("ZkClientConfig field name {} with value '{}' can't be recognized", nodeName,
							nodeValue);
					break;
				}
			}
			ele = ele.getNextSibling();
		}
		config.setExtensionName(this.extensionName);
		return config;
	}

	private void readCooperationConfigs(Node node) {
		if (node == null) {
			return;
		}
		Node item = node.getFirstChild();
		while (item != null) {
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = item.getNodeName();
				switch (nodeName.toLowerCase()) {
				case "zookeeper":
					this.zkClientConfigs.add(readZkClientConfig(item));
					break;
				default:
					getLogger().warn("Cooperation type {} doesn't supported, Extension {}", nodeName,
							this.extensionName);
					break;
				}
			}
			item = item.getNextSibling();
		}
	}

	private void readServerWrapperConfigs(Node node) throws XPathExpressionException {
		if (node == null) {
			return;
		}
		Node item = node.getFirstChild();
		while (item != null) {
			if (item.getNodeType() == 1) {
				Node refAttr = item.getAttributes().getNamedItem("ref");
				PuObjectRO refObj = new PuObject();
				if (refAttr != null) {
					String ref = refAttr.getNodeValue();
					if (ref != null) {
						refObj = this.getRefProperty(ref);
					}
				}
				ServerWrapperType connectionType = ServerWrapperType.fromName(item.getNodeName());
				ServerWrapperConfig config = null;
				switch (connectionType) {
				case HTTP:
					config = new HttpServerWrapperConfig();
					break;
				case RABBITMQ: {
					config = new RabbitMQServerWrapperConfig();
					break;
				}
				default:
					getLogger().warn("ServerWrapperConfig type not supported: " + connectionType);
				}
				if (config != null) {
					config.readPuObject(refObj);
					config.readNode(item);
					serverWrapperConfigs.add(config);
				}
			}
			item = item.getNextSibling();
		}
		for (ServerWrapperConfig config : this.serverWrapperConfigs) {
			config.setExtensionName(this.extensionName);
		}
	}

	private void readGatewayConfigs(Node node) throws XPathExpressionException {
		Node item = node.getFirstChild();
		while (item != null) {
			if (item.getNodeType() == Element.ELEMENT_NODE) {
				GatewayType type = GatewayType.fromName(item.getNodeName());
				if (type != null) {
					GatewayConfig config = null;

					PuObjectRO refObj = new PuObject();
					Node refAttr = item.getAttributes().getNamedItem("ref");
					if (refAttr != null) {
						String ref = refAttr.getNodeValue();
						if (ref != null) {
							refObj = this.getRefProperty(ref);
						}
					}

					switch (type) {
					case ZEROMQ:
						config = new ZeroMQGatewayConfig();
						break;
					case KAFKA:
						config = new KafkaGatewayConfig();
						break;
					case HTTP:
						config = new HttpGatewayConfig();
						break;
					case RABBITMQ:
						config = new RabbitMQGatewayConfig();
						break;
					case SOCKET:
						config = new SocketGatewayConfig();
						break;
					default:
						throw new RuntimeException(type + " gateway doesn't supported now");
					}

					if (config != null) {
						config.setExtensionName(this.extensionName);
						config.readPuObject(refObj);
						config.readNode(item);
						gatewayConfigs.add(config);
					}
				} else {
					getLogger().warn("gateway type not found: {}", item.getNodeName());
				}
			}
			item = item.getNextSibling();
		}
	}

	private PuObject readPuObjectFromNode(Node node) {
		PuObject puo = new PuObject();
		if (node.getAttributes().getNamedItem("ref") != null) {
			String reference = node.getAttributes().getNamedItem("ref").getNodeValue().trim();
			if (this.properties.containsKey(reference)) {
				puo.addAll(this.properties.get(reference).deepClone());
			} else if (this.globalProperties.variableExists(reference)) {
				puo.addAll(this.globalProperties.getPuObject(reference).deepClone());
			} else {
				throw new NullPointerException(
						"Property not found for name: " + reference + ", extension: " + this.getExtensionName());
			}
		}
		puo.addAll(PuObject.fromXML(node));
		return puo;
	}

	private PuObjectRO getRefProperty(String name) {
		if (this.properties.containsKey(name)) {
			return this.properties.get(name);
		} else if (this.globalProperties.variableExists(name)) {
			return this.globalProperties.getPuObject(name);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void readDataSourceConfigs(Node node) throws XPathExpressionException {

		NodeList list = (NodeList) xPath.compile("*").evaluate(node, XPathConstants.NODESET);
		for (int i = 0; i < list.getLength(); i++) {
			Node item = list.item(i);
			if (item.getNodeName().equalsIgnoreCase("sql")) {

				SQLDataSourceConfig config = new SQLDataSourceConfig();

				Node refAttr = item.getAttributes().getNamedItem("ref");
				if (refAttr != null) {
					String ref = refAttr.getNodeValue();
					PuObjectRO refObj = this.getRefProperty(ref);
					if (ref != null) {
						config.readPuObject(refObj);
					}
				}

				Node ele = item.getFirstChild();
				PuObject variables = null;
				while (ele != null) {
					if (ele.getNodeType() == Node.ELEMENT_NODE) {
						String nodeName = ele.getNodeName().toLowerCase();
						switch (nodeName) {
						case "name": {
							config.setName(ele.getTextContent().trim());
							break;
						}
						case "properties":
						case "propertiesfile": {
							try (InputStream is = readFile(ele.getTextContent().trim())) {
								Properties props = new Properties();
								props.load(is);
								config.setProperties(props);
							} catch (Exception e) {
								throw new RuntimeException(
										"Read file error, SQL config at extension name: " + this.getExtensionName(), e);
							}
							break;
						}
						case "variables": {
							variables = PuObject.fromXML(ele);
							break;
						}
						}
					}
					ele = ele.getNextSibling();
				}

				if (variables != null) {
					config.setProperties(variables);
				}

				this.sqlDatasourceConfigs.add(config);
			} else if (item.getNodeName().equalsIgnoreCase("cassandra")) {
				CassandraConfig config = new CassandraConfig();
				Node refAttr = item.getAttributes().getNamedItem("ref");
				if (refAttr != null) {
					String ref = refAttr.getNodeValue();
					PuObjectRO refObj = this.getRefProperty(ref);
					if (ref != null) {
						config.readPuObject(refObj);
					}
				}
				config.readNode(item);
				this.cassandraConfigs.add(config);
			} else if (item.getNodeName().equalsIgnoreCase("hazelcast")) {
				HazelcastConfig config = new HazelcastConfig();
				Node refAttr = item.getAttributes().getNamedItem("ref");
				if (refAttr != null) {
					String ref = refAttr.getNodeValue();
					PuObjectRO refObj = this.getRefProperty(ref);
					if (ref != null) {
						config.readPuObject(refObj);
					}
				}
				config.readNode(item);
				config.setExtensionName(extensionName);
				this.hazelcastConfigs.add(config);
			} else if (item.getNodeName().equalsIgnoreCase("redis")) {
				RedisConfig config = new RedisConfig();
				Node refAttr = item.getAttributes().getNamedItem("ref");
				if (refAttr != null) {
					String ref = refAttr.getNodeValue();
					PuObjectRO refObj = this.getRefProperty(ref);
					if (ref != null) {
						config.readPuObject(refObj);
					}
				}
				config.readNode(item);
				config.setExtensionName(extensionName);
				this.redisConfigs.add(config);
			} else if (item.getNodeName().equalsIgnoreCase("mongodb")) {
				MongoDBConfig config = new MongoDBConfig();

				Node refAttr = item.getAttributes().getNamedItem("ref");
				if (refAttr != null) {
					String ref = refAttr.getNodeValue();
					PuObjectRO refObj = this.getRefProperty(ref);
					if (ref != null) {
						config.readPuObject(refObj);
					}
				}

				Node currNode = item.getFirstChild();
				while (currNode != null) {
					String nodeName = currNode.getNodeName().toLowerCase();
					if (currNode.getNodeType() == 1) {
						switch (nodeName) {
						case "name":
							config.setName(currNode.getTextContent().trim());
							break;
						case "endpoint":
						case "endpoints":
							Object endpoint = null;
							try {
								endpoint = EndpointReader.read(currNode);
							} catch (RuntimeException e) {
								getLogger().error("Invalid endpoint config for mongoDB, extension: {}",
										this.extensionName);
								throw e;
							}
							if (endpoint != null) {
								if (endpoint instanceof HostAndPort) {
									config.addEndpoint((HostAndPort) endpoint);
								} else if (endpoint instanceof Collection) {
									for (HostAndPort hnp : (Collection<HostAndPort>) endpoint) {
										config.addEndpoint(hnp);
									}
								}
							}
							break;
						case "credential":
						case "credentials": {
							Node credentialEntry = currNode.getFirstChild();
							while (credentialEntry != null) {
								if (credentialEntry.getNodeType() == Node.ELEMENT_NODE) {
									String credentialNodeName = credentialEntry.getNodeName().toLowerCase();
									if (credentialNodeName.equalsIgnoreCase("userName")
											|| credentialNodeName.equalsIgnoreCase("password")
											|| credentialNodeName.equalsIgnoreCase("authDB")) {
										// read as single credential
										config.addCredentialConfig(new MongoDBCredentialConfig(credentialEntry));
									} else if (credentialNodeName.equalsIgnoreCase("entry")) {
										// read as multi credential config
										while (credentialEntry != null) {
											if (credentialEntry.getNodeType() == Node.ELEMENT_NODE) {
												credentialNodeName = credentialEntry.getNodeName().toLowerCase();
												if (credentialNodeName.equalsIgnoreCase("entry")) {
													config.addCredentialConfig(
															new MongoDBCredentialConfig(credentialEntry));
												} else {
													getLogger().warn("Invalid credential section: {}, ignored",
															credentialNodeName);
												}
											}
											credentialEntry = credentialEntry.getNextSibling();
										}
										break;
									}
								}
								credentialEntry = credentialEntry.getNextSibling();
							}
							break;
						}
						case "readpreference": {
							config.setReadPreference(new MongoDBReadPreferenceConfig(currNode));
							break;
						}
						default:
							getLogger().warn("Mongodb config section is unrecognized: {}, extension: {}", nodeName,
									this.extensionName);
							break;
						}
					}
					currNode = currNode.getNextSibling();
				}
				this.mongoDBConfigs.add(config);
			} else {
				getLogger().warn("datasource type is not supported: " + item.getNodeName());
			}
		}
	}

	private void readLifeCycleConfigs(Node node) throws XPathExpressionException {
		// read startup config
		if (node == null) {
			return;
		}
		Node item = node.getFirstChild();
		while (item != null) {
			if (item.getNodeType() == 1) {
				LifeCycleConfig config = null;
				if (item.getNodeName().equalsIgnoreCase("handler")) {
					MessageHandlerConfig messageHandlerConfig = new MessageHandlerConfig();
					Object gatewaysObj = xPath.compile("bind/gateway").evaluate(item, XPathConstants.NODESET);
					if (gatewaysObj != null) {
						NodeList gateways = (NodeList) gatewaysObj;
						for (int j = 0; j < gateways.getLength(); j++) {
							messageHandlerConfig.getBindingGateways().add(gateways.item(j).getTextContent().trim());
						}
					}
					config = messageHandlerConfig;
				} else if (item.getNodeName().equalsIgnoreCase("managedobject")) {
					config = new ManagedObjectConfig();
				} else if (item.getNodeName().equalsIgnoreCase("entry")) {
					config = new LifeCycleConfig();
				}

				if (config != null) {
					Node refAttr = item.getAttributes().getNamedItem("ref");
					if (refAttr != null) {
						String ref = refAttr.getNodeValue();
						PuObjectRO refObj = this.getRefProperty(ref);
						if (ref != null) {
							config.readPuObject(refObj);
						}
					}
					String name = ((Node) xPath.compile("name").evaluate(item, XPathConstants.NODE)).getTextContent();
					String handleClass = ((Node) xPath.compile("handle").evaluate(item, XPathConstants.NODE))
							.getTextContent();
					config.setName(name);
					config.setExtensionName(extensionName);
					config.setHandleClass(handleClass);

					Node variableObj = (Node) xPath.compile("variables").evaluate(item, XPathConstants.NODE);
					if (variableObj != null) {
						config.setInitParams(readPuObjectFromNode(variableObj));
					}

					this.lifeCycleConfigs.add(config);
				} else {
					getLogger().warn("lifecycle definition cannot be recognized: " + item);
				}
			}
			item = item.getNextSibling();
		}
	}

	private void readMonitorAgentConfigs(Node node) throws Exception {
		if (node == null) {
			return;
		}
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String nodeName = curr.getNodeName().toLowerCase();
				if (nodeName.equalsIgnoreCase("agent")) {
					MonitorAgentConfig config = new MonitorAgentConfig();
					config.readNode(curr);
					config.setExtensionName(getExtensionName());
					this.monitorAgentConfigs.add(config);
				}
			}
			curr = curr.getNextSibling();
		}
	}

	private void readProducerConfigs(Node node) throws XPathExpressionException {
		if (node == null) {
			return;
		}

		Node item = node.getFirstChild();
		while (item != null) {
			if (item.getNodeType() == Element.ELEMENT_NODE) {
				Node refAttr = item.getAttributes().getNamedItem("ref");
				PuObjectRO refObj = null;
				if (refAttr != null) {
					String ref = refAttr.getNodeValue();
					refObj = this.getRefProperty(ref);
				}

				MessageProducerConfig config = null;

				GatewayType gatewayType = GatewayType.fromName(item.getNodeName());
				switch (gatewayType) {
				case KAFKA:
					config = new KafkaMessageProducerConfig();
					break;
				case RABBITMQ:
					config = new RabbitMQProducerConfig();
					break;
				case HTTP:
					config = new HttpMessageProducerConfig();
					break;
				case ZEROMQ:
					config = new ZeroMQProducerConfig();
					break;
				case SOCKET:
				default:
					throw new UnsupportedTypeException();
				}

				if (config != null) {
					if (refObj != null) {
						config.readPuObject(refObj);
					}
					config.readNode(item);
					if (config.getGatewayType() == null) {
						config.setGatewayType(gatewayType);
					}
					config.setExtensionName(this.extensionName);
					this.producerConfigs.add(config);
				}
			}
			item = item.getNextSibling();
		}
	}

	public PuObjectRO getProperty(String name) {
		return this.properties.get(name);
	}
}
