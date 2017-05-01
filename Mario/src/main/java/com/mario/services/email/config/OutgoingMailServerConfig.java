package com.mario.services.email.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nhb.common.vo.UserNameAndPassword;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OutgoingMailServerConfig {

	public static enum EmailSecurityType {
		NONE, TLS, SSL;

		public static final EmailSecurityType fromName(String name) {
			if (name != null) {
				name = name.trim();
				for (EmailSecurityType value : values()) {
					if (value.name().equalsIgnoreCase(name)) {
						return value;
					}
				}
			}
			return null;
		}
	}

	private String from;
	private String replyTo;
	private String host;
	private int port;
	private EmailSecurityType securityType;

	private UserNameAndPassword authenticator;

	public static OutgoingMailServerConfig read(Node node) {
		OutgoingMailServerConfig config = new OutgoingMailServerConfig();
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String nodeName = curr.getNodeName();
				switch (nodeName.toLowerCase()) {
				case "host":
					config.setHost(curr.getTextContent().trim());
					break;
				case "port":
					config.setPort(Integer.valueOf(curr.getTextContent().trim()));
					break;
				case "secure":
					config.setSecurityType(EmailSecurityType.fromName(curr.getTextContent().trim()));
					break;
				case "from":
					config.setFrom(curr.getTextContent().trim());
					break;
				case "replyto":
					config.setReplyTo(curr.getTextContent().trim());
					break;
				case "authenticator":
					UserNameAndPassword authenticator = new UserNameAndPassword();
					Node ele = curr.getFirstChild();
					while (ele != null) {
						if (ele.getNodeType() == Element.ELEMENT_NODE) {
							String eleName = ele.getNodeName().toLowerCase();
							String eleValue = ele.getTextContent().trim();
							switch (eleName) {
							case "username":
								authenticator.setUserName(eleValue);
								break;
							case "password":
								authenticator.setPassword(eleValue);
								break;
							}
						}
						ele = ele.getNextSibling();
					}
					config.setAuthenticator(authenticator);
					break;
				}
			}
			curr = curr.getNextSibling();
		}

		if (config.getPort() == -1) {
			config.setPort(config.getSecurityType() == EmailSecurityType.NONE ? 25
					: (config.getSecurityType() == EmailSecurityType.SSL ? 465 : 587));
		}
		return config;
	}
}
