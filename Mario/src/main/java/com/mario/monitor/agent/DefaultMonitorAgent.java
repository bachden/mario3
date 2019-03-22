package com.mario.monitor.agent;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;

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

	private MonitorableStatus lastStatus = null;

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
		getLogger().debug("{} execute status checking on target", getName());
		if (response != null) {
			MonitorableStatus status = response.getStatus();
			MonitorAlertStatusConfig alertConfig = getAlertConfig().getStatusToConfigs().get(status);

			// if response status == OK, and auto send recovery is
			if (alertConfig == null //
					&& response.getStatus() == MonitorableStatus.OK //
					&& this.getAlertConfig().isAutoSendRecovery() //
					&& (lastStatus == MonitorableStatus.CRITICAL || lastStatus == MonitorableStatus.WARNING)) {
				// try to get alert config for RECOVERY status
				alertConfig = getAlertConfig().getStatusToConfigs().get(MonitorableStatus.RECOVERY);
				if (alertConfig == null) {
					// if RECOVERY status didn't configured, try to get alert
					// config from lastStatus
					alertConfig = getAlertConfig().getStatusToConfigs().get(lastStatus);
				}
			}

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
					sendAlert(response, emails, phoneNumbers, telegramIds, servicesConfig);
				}
			}
			lastStatus = status;
		}
	}

	private void sendAlert(MonitorableResponse response, Collection<String> emails, Collection<String> phoneNumbers,
			Collection<String> telegramIds, MonitorAlertServicesConfig servicesConfig) {
		if (emails.size() > 0) {
			sendViaEmail(response, emails, servicesConfig);
		}
		if (phoneNumbers.size() > 0) {
			sendViaSMS(response, phoneNumbers, servicesConfig);
		}
		if (telegramIds.size() > 0) {
			sendViaTelegram(response, telegramIds, servicesConfig);
		}
	}

	private void sendViaEmail(MonitorableResponse response, Collection<String> emails,
			MonitorAlertServicesConfig servicesConfig) {
		DefaultEmailEnvelope envelope = new DefaultEmailEnvelope();
		envelope.setContent(response.getMessage());
		envelope.setSubject("[" + getName() + " ALERT] " + response.getStatus().name());
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

	private void sendViaSMS(MonitorableResponse response, Collection<String> phoneNumbers,
			MonitorAlertServicesConfig servicesConfig) {
		String message = "[" + this.getName() + " ALERT] " + response.getStatus().name() + "\n" + response.getMessage();
		for (String smsServiceName : servicesConfig.getSmsServices()) {
			SmsService<?> smsService = getApi().getSmsService(smsServiceName);
			if (smsService != null) {
				try {
					smsService.send(message, phoneNumbers);
				} catch (Exception e) {
					getLogger().error("Error while sending sms: ", e);
				}
			}
		}
	}

	private void sendViaTelegram(MonitorableResponse response, Collection<String> telegramIds,
			MonitorAlertServicesConfig servicesConfig) {
		String messageStr = "[" + this.getName() + " ALERT] " + response.getStatus().name() + "\n"
				+ response.getMessage();
		SendMessage message = new SendMessage();
		message.setText(messageStr);
		for (String telegramBotName : servicesConfig.getTelegramBots()) {
			TelegramBot bot = getApi().getTelegramBot(telegramBotName);
			if (bot != null) {
				for (String telegramPhoneNumber : telegramIds) {
					try {
						long chatId = bot.getChatId(telegramPhoneNumber);
						if (chatId > 0) {
							message.setChatId(chatId);
							((AbsSender) bot).execute(message);
						} else {
							getLogger().warn("Cannot fetch telegram chat id for phone number {}", telegramPhoneNumber);
						}
					} catch (Exception e) {
						getLogger().error("Send message via telegram bot error", e);
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
