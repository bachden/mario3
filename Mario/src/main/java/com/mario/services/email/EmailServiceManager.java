package com.mario.services.email;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nhb.common.Loggable;

public class EmailServiceManager implements Loggable {

	private final Map<String, EmailService> emailServices = new ConcurrentHashMap<>();

	public void register(EmailService emailService) {
		this.emailServices.put(emailService.getName(), emailService);
	}

	public EmailService getEmailService(String name) {
		return this.emailServices.get(name);
	}

	public void shutdown() {
		for (EmailService emailService : this.emailServices.values()) {
			try {
				emailService.destroy();
			} catch (Exception e) {
				e.printStackTrace();
				getLogger().error("Error while trying to destroy email service " + emailService.getName());
			}
		}
	}

}
