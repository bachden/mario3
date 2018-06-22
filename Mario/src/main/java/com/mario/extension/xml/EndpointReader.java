package com.mario.extension.xml;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import com.nhb.common.vo.HostAndPort;

public class EndpointReader {

	protected static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	protected static final XPath xPath = XPathFactory.newInstance().newXPath();

	private static HostAndPort readEntry(Node node) {
		if (node != null) {
			HostAndPort hostAndPort = new HostAndPort();
			Node currNode = node.getFirstChild();
			while (currNode != null) {
				if (currNode.getNodeType() == 1) {
					String textContent = currNode.getTextContent().trim();
					if (currNode.getNodeName().equalsIgnoreCase("host")) {
						hostAndPort.setHost(textContent);
					} else if (currNode.getNodeName().equalsIgnoreCase("port")) {
						hostAndPort.setPort(Integer.valueOf(textContent));
					} else if (currNode.getNodeName().equalsIgnoreCase("master")
							|| currNode.getNodeName().equalsIgnoreCase("ismaster")) {
						hostAndPort.setMaster(Boolean.valueOf(textContent));
					}
				} else if (currNode.getNodeType() == 3) {
					hostAndPort = HostAndPort.fromString(currNode.getTextContent().trim());
					break;
				}
				currNode = currNode.getNextSibling();
			}
			return hostAndPort;
		}
		return null;
	}

	public static Object read(Node node) {
		Node currNode = node.getFirstChild();
		while (currNode != null) {
			String nodeName = currNode.getNodeName();
			String nodeValue = currNode.getNodeValue().trim();
			switch (currNode.getNodeType()) {
			case Node.COMMENT_NODE:
				currNode = currNode.getNextSibling();
				break;
			case 1: // if first child is element
				if (nodeName.equalsIgnoreCase("entry")) {
					// read entries
					Collection<HostAndPort> results = new HashSet<>();
					while (currNode != null) {
						if (currNode.getNodeType() == 1) {
							results.add(readEntry(currNode));
						}
						currNode = currNode.getNextSibling();
					}
					return results;
				} else if (nodeName.equalsIgnoreCase("host") || nodeName.equalsIgnoreCase("port")) {
					HostAndPort hostAndPort = new HostAndPort();
					while (currNode != null) {
						String content = currNode.getTextContent().trim();
						if (nodeName.equalsIgnoreCase("host")) {
							if (hostAndPort.getHost() != null) {
								throw new RuntimeException("Multi config is not allowed for host");
							}
							if (currNode.getFirstChild().getNodeType() == 3) {
								hostAndPort.setHost(content);
							} else {
								throw new RuntimeException("Invalid host config: " + currNode.getTextContent());
							}
						} else if (nodeName.equalsIgnoreCase("port")) {
							if (hostAndPort.getPort() > 0) {
								throw new RuntimeException("Multi config is not allowed for port");
							}
							if (currNode.getFirstChild().getNodeType() == 3) {
								hostAndPort.setPort(Integer.valueOf(content));
							} else {
								throw new RuntimeException("Invalid port config: " + currNode.getTextContent());
							}
						} else if (nodeName.equalsIgnoreCase("master") || nodeName.equalsIgnoreCase("ismaster")) {
							if (currNode.getFirstChild().getNodeType() == 3) {
								hostAndPort.setMaster(Boolean.valueOf(content));
							} else {
								throw new RuntimeException("Invalid master config: " + currNode.getTextContent());
							}
						}
						currNode = currNode.getNextSibling();
					}
					return hostAndPort;
				} else {
					throw new RuntimeException("Invalid tag name: " + nodeName);
				}
			case 2: // if first element is attribute
				HostAndPort hostAndPort = new HostAndPort();
				while (currNode != null) {
					if (currNode.getNodeType() != 2) {
						throw new RuntimeException(
								"Invalid config, host and port config via attribute must both is attribute and endpoint node cannot has child nodes");
					}
					if (nodeName.equalsIgnoreCase("host")) {
						if (hostAndPort.getHost() != null) {
							throw new RuntimeException("Multi config is not allowed for host");
						}
						hostAndPort.setHost(nodeValue);
					} else if (nodeName.equalsIgnoreCase("port")) {
						if (hostAndPort.getPort() > 0) {
							throw new RuntimeException("Multi config is not allowed for port");
						}
						hostAndPort.setPort(Integer.valueOf(nodeValue));
					} else if (nodeName.equalsIgnoreCase("master") || nodeName.equalsIgnoreCase("ismaster")) {
						hostAndPort.setMaster(Boolean.valueOf(nodeValue));
					}
					currNode = currNode.getNextSibling();
				}
				return hostAndPort;
			case 3: // if first element is text
				String textContent = nodeValue;
				if (textContent.isEmpty()) {
					currNode = currNode.getNextSibling();
					continue;
				}
				if (textContent.indexOf(",") >= 0) {
					String[] arr = textContent.split(",");
					Collection<HostAndPort> results = new HashSet<>();
					for (String str : arr) {
						HostAndPort endpoint = HostAndPort.fromString(str);
						if (endpoint != null) {
							results.add(endpoint);
						}
					}
					return results;
				} else {
					return HostAndPort.fromString(textContent);
				}
			}
		}
		return null;
	}
}
