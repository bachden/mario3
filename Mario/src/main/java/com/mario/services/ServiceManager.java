package com.mario.services;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.mario.api.MarioApiFactory;
import com.mario.entity.NamedLifeCycle;
import com.mario.entity.Pluggable;
import com.mario.extension.ExtensionManager;
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
import com.mario.services.telegram.TelegramBot;
import com.mario.services.telegram.TelegramBotManager;
import com.mario.services.telegram.TelegramBotRegisterStrategy;
import com.mario.services.telegram.TelegramBotType;
import com.mario.services.telegram.bots.DefaultTelegramLongPollingBot;
import com.mario.services.telegram.bots.DefaultTelegramWebhookBot;
import com.mario.services.telegram.storage.TelegramBotStorageConfig;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuObject;

public class ServiceManager implements Loggable {

	private final SmsServiceManager smsManager = new SmsServiceManager();
	private final EmailServiceManager emailManager = new EmailServiceManager();
	private final TelegramBotManager telegramBotManager = new TelegramBotManager();

	private final List<SmsServiceConfig> smsConfigs = new ArrayList<>();
	private final List<EmailServiceConfig> emailConfigs = new ArrayList<>();

	private void readSmsConfig(Node node, String extensionName) {
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
		builder.extensionName(extensionName);
		this.smsConfigs.add(builder.build());
	}

	private void readEmailConfig(Node node, String extensionName) {
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
		builder.extensionName(extensionName);
		this.emailConfigs.add(builder.build());
	}

	private void readTelegarmBotConfig(Node node) {
		TelegramBotType botType = null;
		String name = null;
		String botToken = null;
		String botUsername = null;
		TelegramBotStorageConfig storageConfig = null;
		TelegramBotRegisterStrategy registerStrategy = null;
		boolean sendAckEvenRegistered = false;
		boolean autoSendAck = false;

		Node ele = node.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == Element.ELEMENT_NODE) {
				String eleName = ele.getNodeName().toLowerCase();
				String eleValue = ele.getTextContent().trim();

				switch (eleName) {
				case "name":
					name = eleValue;
					break;
				case "botusername":
					botUsername = eleValue;
					break;
				case "bottoken":
					botToken = eleValue;
					break;
				case "register":
					registerStrategy = TelegramBotRegisterStrategy.fromName(eleValue);
					break;
				case "type":
					botType = TelegramBotType.fromName(eleValue);
					break;
				case "storage":
					storageConfig = TelegramBotStorageConfig.read(ele);
					break;
				case "sendackevenregistered":
					sendAckEvenRegistered = Boolean.valueOf(eleValue);
					break;
				case "autosendack":
					autoSendAck = Boolean.valueOf(eleValue);
					break;
				}

			}
			ele = ele.getNextSibling();
		}

		if (botType == null) {
			throw new RuntimeException("Telegram bot config must contain type info");
		}

		if (name == null) {
			throw new RuntimeException("Telegram bot config must contain name info");
		}

		if (storageConfig == null) {
			throw new RuntimeException("Storage config cannot be null");
		}

		if (registerStrategy == null) {
			registerStrategy = TelegramBotRegisterStrategy.IMMEDIATELY;
		}

		TelegramBot bot = null;
		switch (botType) {
		case LONG_POLLING:
			bot = new DefaultTelegramLongPollingBot(name, storageConfig, registerStrategy, botToken, botUsername);
			break;
		case WEBHOOK:
			bot = new DefaultTelegramWebhookBot(name, storageConfig, registerStrategy, botToken, botUsername);
			break;
		}

		if (bot != null) {
			bot.setSendAckEvenRegistered(sendAckEvenRegistered);
			bot.setAutoSendAck(autoSendAck);
			this.telegramBotManager.register(bot);
		}
	}

	public void readFromXml(Node node, String extensionName) {
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String currName = curr.getNodeName();
				switch (currName.toLowerCase()) {
				case "email":
					readEmailConfig(curr, extensionName);
					break;
				case "sms":
					readSmsConfig(curr, extensionName);
					break;
				case "telegram":
				case "telegrambot":
					readTelegarmBotConfig(curr);
					break;
				default:
					getLogger().warn("Service name cannot be recognized " + currName);
					break;
				}
			}
			curr = curr.getNextSibling();
		}
	}

	public void init(MarioApiFactory apiFactory, ExtensionManager extManager) {
		for (EmailServiceConfig emailConfig : this.emailConfigs) {
			EmailService emailService = null;
			if (emailConfig.getHandle() != null) {
				try {
					emailService = extManager.newInstance(emailConfig.getExtensionName(), emailConfig.getHandle());
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
				SmsService<?> smsService = extManager.newInstance(smsConfig.getExtensionName(), smsConfig.getHandle());
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

		this.telegramBotManager.init(apiFactory);
	}

	@SuppressWarnings("unchecked")
	public <R> SmsService<R> getSmsService(String name) {
		return (SmsService<R>) this.smsManager.getSmsService(name);
	}

	public EmailService getEmailService(String name) {
		return this.emailManager.getEmailService(name);
	}

	public TelegramBot getTelegramBot(String name) {
		return this.telegramBotManager.getBot(name);
	}

	public void shutdown() {
		this.smsManager.shutdown();
		this.emailManager.shutdown();
	}
}
