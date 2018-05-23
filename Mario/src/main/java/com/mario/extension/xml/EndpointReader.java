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
					if (currNode.getNodeName().equalsIgnoreCase("host")) {
						hostAndPort.setHost(currNode.getTextContent().trim());
					} else if (currNode.getNodeName().equalsIgnoreCase("port")) {
						hostAndPort.setPort(Integer.valueOf(currNode.getTextContent()));
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
			switch (currNode.getNodeType()) {
			case Node.COMMENT_NODE:
				currNode = currNode.getNextSibling();
				break;
			case 1: // if first child is element
				if (currNode.getNodeName().equalsIgnoreCase("entry")) {
					// read entries
					Collection<HostAndPort> results = new HashSet<>();
					while (currNode != null) {
						if (currNode.getNodeType() == 1) {
							results.add(readEntry(currNode));
						}
						currNode = currNode.getNextSibling();
					}
					return results;
				} else if (currNode.getNodeName().equalsIgnoreCase("host")
						|| currNode.getNodeName().equalsIgnoreCase("port")) {
					HostAndPort hostAndPort = new HostAndPort();
					while (currNode != null) {
						if (currNode.getNodeName().equalsIgnoreCase("host")) {
							if (hostAndPort.getHost() != null) {
								throw new RuntimeException("Multi config is not allowed for host");
							}
							if (currNode.getFirstChild().getNodeType() == 3) {
								hostAndPort.setHost(currNode.getTextContent().trim());
							} else {
								throw new RuntimeException("Invalid host config: " + currNode.getTextContent());
							}
						} else if (currNode.getNodeName().equalsIgnoreCase("port")) {
							if (hostAndPort.getPort() > 0) {
								throw new RuntimeException("Multi config is not allowed for port");
							}
							if (currNode.getFirstChild().getNodeType() == 3) {
								hostAndPort.setPort(Integer.valueOf(currNode.getTextContent().trim()));
							} else {
								throw new RuntimeException("Invalid port config: " + currNode.getTextContent());
							}
						}
						currNode = currNode.getNextSibling();
					}
					return hostAndPort;
				} else {
					throw new RuntimeException("Invalid tag name: " + currNode.getNodeName());
				}
			case 2: // if first element is attribute
				HostAndPort hostAndPort = new HostAndPort();
				while (currNode != null) {
					if (currNode.getNodeType() != 2) {
						throw new RuntimeException(
								"Invalid config, host and port config via attribute must both is attribute and endpoint node cannot has child nodes");
					}
					if (currNode.getNodeName().equalsIgnoreCase("host")) {
						if (hostAndPort.getHost() != null) {
							throw new RuntimeException("Multi config is not allowed for host");
						}
						hostAndPort.setHost(currNode.getNodeValue().trim());
					} else if (currNode.getNodeName().equalsIgnoreCase("port")) {
						if (hostAndPort.getPort() > 0) {
							throw new RuntimeException("Multi config is not allowed for port");
						}
						hostAndPort.setPort(Integer.valueOf(currNode.getNodeValue().trim()));
					}
					currNode = currNode.getNextSibling();
				}
				return hostAndPort;
			case 3: // if first element is text
				String textContent = currNode.getNodeValue().trim();
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
