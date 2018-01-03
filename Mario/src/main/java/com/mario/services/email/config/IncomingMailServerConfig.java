package com.mario.services.email.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nhb.common.vo.UserNameAndPassword;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IncomingMailServerConfig {

	public static enum EmailProtocol {
		POP, IMAP;

		public static final EmailProtocol fromName(String name) {
			if (name != null) {
				for (EmailProtocol value : values()) {
					if (value.name().equalsIgnoreCase(name)) {
						return value;
					}
				}
			}
			return null;
		}
	}

	private String host;
	private int port = -1;
	private boolean secure = false;
	private EmailProtocol protocol = null;

	private UserNameAndPassword authenticator;

	public static IncomingMailServerConfig read(Node node) {
		IncomingMailServerConfig config = new IncomingMailServerConfig();
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String nodeName = curr.getNodeName();
				switch (nodeName.toLowerCase()) {
				case "protocol":
					config.setProtocol(EmailProtocol.fromName(curr.getTextContent().trim()));
					break;
				case "host":
					config.setHost(curr.getTextContent().trim());
					break;
				case "port":
					config.setPort(Integer.valueOf(curr.getTextContent().trim()));
					break;
				case "secure":
					config.setSecure(Boolean.valueOf(curr.getTextContent().trim()));
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

		if (config.protocol == null) {
			config.protocol = EmailProtocol.IMAP;
		}

		if (config.getPort() == -1) {
			switch (config.getProtocol()) {
			case IMAP:
				config.setPort(config.isSecure() ? 993 : 143);
				break;
			case POP:
				config.setPort(config.isSecure() ? 995 : 110);
				break;
			}
		}
		return config;
	}
}
