package com.mario.extension.xml;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import com.nhb.common.vo.UserNameAndPassword;

public class CredentialReader {

	protected static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	protected static final XPath xPath = XPathFactory.newInstance().newXPath();

	private static UserNameAndPassword readEntry(Node node) {
		if (node != null) {
			UserNameAndPassword userNameAndPassword = new UserNameAndPassword();
			Node currNode = node.getFirstChild();
			while (currNode != null) {
				if (currNode.getNodeType() == 1) {
					if (currNode.getNodeName().equalsIgnoreCase("user")
							|| currNode.getNodeName().equalsIgnoreCase("username")) {
						userNameAndPassword.setUserName(currNode.getTextContent().trim());
					} else if (currNode.getNodeName().equalsIgnoreCase("pass")
							|| currNode.getNodeName().equalsIgnoreCase("password")) {
						userNameAndPassword.setPassword(currNode.getTextContent().trim());
					}
				}
				currNode = currNode.getNextSibling();
			}
			return userNameAndPassword;
		}
		return null;
	}

	public static Object read(Node node) {
		Node currNode = node.getFirstChild();
		while (currNode != null) {
			if (currNode.getNodeType() == 1) {
				if (currNode.getNodeName().equalsIgnoreCase("entry")) {
					// read entries
					Collection<UserNameAndPassword> results = new HashSet<>();
					while (currNode != null) {
						if (currNode.getNodeType() == 1) {
							results.add(readEntry(currNode));
						}
						currNode = currNode.getNextSibling();
					}
					return results;
				} else if (currNode.getNodeName().equalsIgnoreCase("user")
						|| currNode.getNodeName().equalsIgnoreCase("username")
						|| currNode.getNodeName().equalsIgnoreCase("pass")
						|| currNode.getNodeName().equalsIgnoreCase("password")) {
					UserNameAndPassword userNameAndPassword = new UserNameAndPassword();
					while (currNode != null) {
						String value = currNode.getTextContent().trim();
						if (currNode.getNodeName().equalsIgnoreCase("user")
								|| currNode.getNodeName().equalsIgnoreCase("username")) {
							if (currNode.getFirstChild().getNodeType() == 3) {
								userNameAndPassword.setUserName(value);
							}
						} else if (currNode.getNodeName().equalsIgnoreCase("pass")
								|| currNode.getNodeName().equalsIgnoreCase("password")) {
							if (currNode.getFirstChild().getNodeType() == 3) {
								userNameAndPassword.setPassword(value);
							}
						}
						currNode = currNode.getNextSibling();
					}
					return userNameAndPassword;
				} else {
					throw new RuntimeException("Invalid tag name: " + currNode.getNodeName());
				}
			}
			currNode = currNode.getNextSibling();
		}

		return null;
	}
}
