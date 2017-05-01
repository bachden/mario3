package com.mario.services;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.mario.api.MarioApiFactory;
import com.mario.entity.NamedLifeCycle;
import com.mario.entity.Pluggable;
import com.mario.services.email.DefaultEmailService;
import com.mario.services.email.EmailService;
import com.mario.services.email.EmailServiceConfigurable;
import com.mario.services.email.EmailServiceManager;
import com.mario.services.email.config.EmailServiceConfig;
import com.mario.services.email.config.EmailServiceConfig.EmailServiceConfigBuilder;
import com.mario.services.email.config.IncomingMailServerConfig;
import com.mario.services.email.config.OutgoingMailServerConfig;
import com.mario.services.sms.SmsService;
import com.mario.services.sms.SmsServiceManager;
import com.mario.services.sms.config.SmsServiceConfig;
import com.mario.services.sms.config.SmsServiceConfig.SmsServiceConfigBuilder;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuObject;

public class ServiceManager implements Loggable {

	private final SmsServiceManager smsManager = new SmsServiceManager();
	private final EmailServiceManager emailManager = new EmailServiceManager();

	private final List<SmsServiceConfig> smsConfigs = new ArrayList<>();
	private final List<EmailServiceConfig> emailConfigs = new ArrayList<>();

	private void readSmsConfig(Node node) {
		Node curr = node.getFirstChild();
		SmsServiceConfigBuilder builder = SmsServiceConfig.builder();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String nodeName = curr.getNodeName();
				switch (nodeName.toLowerCase()) {
				case "name":
					builder.name(curr.getTextContent().trim());
					break;
				case "handle":
					builder.handle(curr.getTextContent().trim());
					break;
				case "variables":
					builder.initParams(PuObject.fromXML(curr));
					break;
				}
			}
			curr = curr.getNextSibling();
		}
		this.smsConfigs.add(builder.build());
	}

	private void readEmailConfig(Node node) {
		Node curr = node.getFirstChild();
		EmailServiceConfigBuilder builder = EmailServiceConfig.builder();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String nodeName = curr.getNodeName();
				switch (nodeName) {
				case "name":
					builder.name(curr.getTextContent().trim());
					break;
				case "handle":
					builder.handle(curr.getTextContent().trim());
					break;
				case "variables":
					builder.initParams(PuObject.fromXML(curr));
					break;
				case "incoming":
					builder.incomingConfig(IncomingMailServerConfig.read(curr));
					break;
				case "outgoing":
					builder.outgoingConfig(OutgoingMailServerConfig.read(curr));
					break;
				}
			}
			curr = curr.getNextSibling();
		}
		this.emailConfigs.add(builder.build());
	}

	public void readFromXml(Node node) {
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String currName = curr.getNodeName();
				switch (currName.toLowerCase()) {
				case "email":
					readEmailConfig(curr);
					break;
				case "sms":
					readSmsConfig(curr);
					break;
				default:
					getLogger().warn("Service name cannot be recognized " + currName);
					break;
				}
			}
			curr = curr.getNextSibling();
		}
	}

	public void init(MarioApiFactory apiFactory) {
		for (EmailServiceConfig emailConfig : this.emailConfigs) {
			EmailService emailService = null;
			if (emailConfig.getHandle() != null) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends EmailService> clazz = (Class<? extends EmailService>) Class
							.forName(emailConfig.getHandle());
					emailService = clazz.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(
							"Cannot init email service with handle class " + emailConfig.getHandle());
				}
			} else {
				emailService = new DefaultEmailService();
			}
			if (emailService instanceof NamedLifeCycle) {
				((NamedLifeCycle) emailService).setName(emailConfig.getName());
			}
			if (emailService instanceof Pluggable) {
				((Pluggable) emailService).setApi(apiFactory.newApi());
			}
			if (emailService instanceof EmailServiceConfigurable) {
				((EmailServiceConfigurable) emailService).setIncomingConfig(emailConfig.getIncomingConfig());
				((EmailServiceConfigurable) emailService).setOutgoingConfig(emailConfig.getOutgoingConfig());
			}
			emailService.init(emailConfig.getInitParams() == null ? new PuObject() : emailConfig.getInitParams());

			this.emailManager.register(emailService);
		}

		for (SmsServiceConfig smsConfig : this.smsConfigs) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends SmsService> clazz = (Class<? extends SmsService>) Class.forName(smsConfig.getHandle());
				SmsService smsService = clazz.newInstance();
				if (smsService instanceof NamedLifeCycle) {
					((NamedLifeCycle) smsService).setName(smsConfig.getName());
				}
				if (smsService instanceof Pluggable) {
					((Pluggable) smsService).setApi(apiFactory.newApi());
				}
				smsService.init(smsConfig.getInitParams() == null ? new PuObject() : smsConfig.getInitParams());
				this.smsManager.register(smsService);
			} catch (Exception e) {
				throw new RuntimeException("Error while create SmsService instance", e);
			}
		}
	}

	public SmsService getSmsService(String name) {
		return this.smsManager.getSmsService(name);
	}

	public EmailService getEmailService(String name) {
		return this.emailManager.getEmailService(name);
	}

	public void shutdown() {
		this.smsManager.shutdown();
		this.emailManager.shutdown();
	}
}
