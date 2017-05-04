package com.mario.services.telegram.bots;

import java.util.concurrent.atomic.AtomicBoolean;

import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramWebhookBot;

import com.mario.api.MarioApi;
import com.mario.services.telegram.TelegramBot;
import com.mario.services.telegram.TelegramBotType;
import com.mario.services.telegram.event.TelegramEvent;
import com.mario.services.telegram.storage.TelegramBotStorage;
import com.mario.services.telegram.storage.TelegramBotStorageConfig;
import com.mario.services.telegram.storage.impl.TelegramBotFileStorage;
import com.nhb.common.Loggable;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventDispatcher;
import com.nhb.eventdriven.EventHandler;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

import lombok.Getter;
import lombok.Setter;

public class DefaultTelegramWebhookBot extends TelegramWebhookBot implements EventDispatcher, TelegramBot, Loggable {

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String botUsername;

	@Getter
	@Setter
	private String botPath;

	@Getter
	@Setter
	private String botToken;

	private AtomicBoolean registered = new AtomicBoolean(false);

	private final TelegramBotStorageConfig storageConfig;
	private TelegramBotStorage storage;

	@Setter
	@Getter
	private MarioApi api;

	private EventDispatcher eventDispatcher = new BaseEventDispatcher();

	public DefaultTelegramWebhookBot(String name, TelegramBotStorageConfig storageConfig) {
		this.setName(name);
		this.storageConfig = storageConfig;
	}

	public DefaultTelegramWebhookBot(String name, TelegramBotStorageConfig storageConfig, String botToken,
			String botUsername) {
		this(name, storageConfig);
		this.setBotUsername(botUsername);
		this.setBotToken(botToken);
	}

	public DefaultTelegramWebhookBot(String name, TelegramBotStorageConfig storageConfig, String botToken) {
		this(name, storageConfig);
		this.setBotToken(botToken);
	}

	@Override
	public void init() {
		switch (this.storageConfig.getType()) {
		case FILE:
			this.storage = new TelegramBotFileStorage(this.getBotUsername(), storageConfig.getFilePath());
			break;
		default:
			throw new RuntimeException("Storage type not supported: " + storageConfig.getType());
		}
	}

	@Override
	public TelegramBotType getType() {
		return TelegramBotType.WEBHOOK;
	}

	@Override
	public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
		if (update.hasMessage()) {
			Long chatId = update.getMessage().getChatId();
			String phoneNumber = update.getMessage().getContact().getPhoneNumber();
			if (phoneNumber != null && chatId > 0) {
				if (this.storage.saveChatId(phoneNumber, chatId)) {
					getLogger().info("TelegramBot name {} --> saved phone number {} and chat id {}",
							this.botUsername, phoneNumber, chatId);
				}
			}
		}
		this.dispatchEvent(TelegramEvent.newUpdateEvent(update));
		return null;
	}

	public void addEventListener(String eventType, EventHandler listener) {
		eventDispatcher.addEventListener(eventType, listener);
	}

	public void removeEventListener(String eventType, EventHandler listener) {
		eventDispatcher.removeEventListener(eventType, listener);
	}

	/**
	 * Method use for internal only, do not invoke it
	 */
	@Deprecated
	public void dispatchEvent(Event event) {
		if (!(event instanceof TelegramEvent)) {
			throw new IllegalArgumentException("Expected event type " + TelegramEvent.class.getName());
		}

		if (event.getCurrentTarget() == null) {
			event.setCurrentTarget(this);
		}
		if (event.getTarget() == null) {
			event.setTarget(this);
		}
		eventDispatcher.dispatchEvent(event);
	}

	@Override
	@Deprecated
	public void removeAllEventListener() {
		throw new UnsupportedOperationException("Method not supported");
	}

	@Override
	@Deprecated
	public void dispatchEvent(String eventType, Object... data) {
		throw new UnsupportedOperationException("Method not supported");
	}

	@Override
	public boolean isRegistered() {
		return this.registered.get();
	}

	@Override
	public boolean setRegistered() {
		return this.registered.compareAndSet(false, true);
	}

	@Override
	public long getChatId(String phoneNumber) {
		return this.storage.getChatId(phoneNumber);
	}
}
