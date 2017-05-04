package com.mario.monitor.agent;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.AbsSender;

import com.mario.contact.Contact;
import com.mario.monitor.MonitorableResponse;
import com.mario.monitor.MonitorableStatus;
import com.mario.monitor.config.MonitorAlertRecipientsConfig;
import com.mario.monitor.config.MonitorAlertServicesConfig;
import com.mario.monitor.config.MonitorAlertStatusConfig;
import com.mario.schedule.ScheduledCallback;
import com.mario.schedule.ScheduledFuture;
import com.mario.schedule.distributed.DistributedScheduler;
import com.mario.schedule.distributed.exception.DistributedScheduleException;
import com.mario.services.email.DefaultEmailEnvelope;
import com.mario.services.email.EmailService;
import com.mario.services.sms.SmsService;
import com.mario.services.telegram.TelegramBot;
import com.nhb.common.Loggable;

public class DefaultMonitorAgent extends BaseMonitorAgent implements Loggable {

	private ScheduledFuture monitorScheduledId;

	private ExecutorService executor = Executors.newCachedThreadPool();

	private String distributedSchedulerName;

	public DefaultMonitorAgent(String distributedSchedulerName) {
		this.distributedSchedulerName = distributedSchedulerName;
	}

	private Collection<Contact> extractContactListFromRecipientsConfig(MonitorAlertRecipientsConfig recipientsConfig) {
		Collection<Contact> results = new HashSet<>();
		for (String contactName : recipientsConfig.getContacts()) {
			Contact contact = getApi().getContactBook().getContact(contactName);
			if (contact != null) {
				results.add(contact);
			}
		}
		for (String group : recipientsConfig.getGroups()) {
			results.addAll(getApi().getContactBook().getContactByGroup(group));
		}
		return results;
	}

	@Override
	public void executeCheck() {
		MonitorableResponse response = getTarget().checkStatus();
		getLogger().debug("{} is execute check status on target", getName());
		if (response != null) {
			MonitorableStatus status = response.getStatus();
			MonitorAlertStatusConfig alertConfig = getAlertConfig().getStatusToConfigs().get(status);
			if (alertConfig != null) {
				Collection<Contact> contacts = extractContactListFromRecipientsConfig(
						alertConfig.getRecipientsConfig());
				if (contacts != null && contacts.size() > 0) {

					Collection<String> emails = new HashSet<>();
					Collection<String> phoneNumbers = new HashSet<>();
					Collection<String> telegramIds = new HashSet<>();

					for (Contact contact : contacts) {
						if (contact.getEmail() != null) {
							emails.add(contact.getEmail());
						}
						if (contact.getPhoneNumber() != null) {
							phoneNumbers.add(contact.getPhoneNumber());
						}
						if (contact.getTelegramId() != null) {
							telegramIds.add(contact.getTelegramId());
						}
					}

					MonitorAlertServicesConfig servicesConfig = alertConfig.getServicesConfig();
					if (emails.size() > 0) {
						DefaultEmailEnvelope envelope = new DefaultEmailEnvelope();
						envelope.setContent(response.getMessage());
						envelope.setSubject("[" + getName() + " ALERT] " + status.name());
						envelope.getTo().addAll(emails);

						for (String emailServiceName : servicesConfig.getEmailServices()) {
							EmailService emailService = getApi().getEmailService(emailServiceName);
							if (emailService != null) {
								executor.submit(new Runnable() {

									@Override
									public void run() {
										try {
											emailService.send(envelope);
										} catch (Exception e) {
											getLogger().error("Error while sending email: ", e);
										}
									}
								});
							}
						}
					}

					if (phoneNumbers.size() > 0) {
						for (String smsServiceName : servicesConfig.getSmsServices()) {
							SmsService smsService = getApi().getSmsService(smsServiceName);
							if (smsService != null) {
								try {
									smsService.send(response.getMessage(), phoneNumbers);
								} catch (Exception e) {
									getLogger().error("Error while sending sms: ", e);
								}
							}
						}
					}

					if (telegramIds.size() > 0) {
						for (String telegramBotName : servicesConfig.getTelegramBots()) {
							TelegramBot bot = getApi().getTelegramBot(telegramBotName);
							if (bot != null) {
								for (String telegramPhoneNumber : telegramIds) {
									try {
										long chatId = bot.getChatId(telegramPhoneNumber);
										if (chatId > 0) {
											SendMessage message = new SendMessage();
											message.setChatId(chatId);
											message.setText(response.getMessage());
											((AbsSender) bot).sendMessage(message);
										} else {
											getLogger().warn("Cannot fetch telegram chat id for phone number {}",
													telegramPhoneNumber);
										}
									} catch (Exception e) {
										getLogger().error("Send message via telegram bot error", e);
									}
								}
							}
						}
					}
				}
			}
		}

	}

	@Override
	public void start() {
		getLogger().info("Start monitor agent {}", this.getName());

		if (this.distributedSchedulerName == null) {
			this.monitorScheduledId = getApi().getScheduler().scheduleAtFixedRate(getInterval(), getInterval(),
					new ScheduledCallback() {

						@Override
						public void call() {
							executeCheck();
						}
					});
		} else {
			DistributedScheduler scheduler = this.getApi().getDistributedScheduler(this.distributedSchedulerName);
			String taskName = this.getName() + "_monitor_agent_monitoring_task";
			try {
				MonitorAgentDistributedRunnable runner = new MonitorAgentDistributedRunnable();
				runner.setMonitorAgentName(this.getName());
				scheduler.scheduleAtFixedRate(taskName, runner, this.getInterval(), this.getInterval(),
						TimeUnit.MILLISECONDS);
			} catch (DistributedScheduleException e) {
				getLogger().info("Task was scheduled in another node, ignore...");
			}
		}
	}

	@Override
	public void stop() {
		if (this.monitorScheduledId != null) {
			this.monitorScheduledId.cancel();
			this.monitorScheduledId = null;
		}
	}

}
