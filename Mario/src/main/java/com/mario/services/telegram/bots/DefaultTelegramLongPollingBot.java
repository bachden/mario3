package com.mario.services.telegram.bots;

import java.util.concurrent.atomic.AtomicBoolean;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.mario.api.MarioApi;
import com.mario.services.telegram.TelegramBot;
import com.mario.services.telegram.TelegramBotType;
import com.mario.services.telegram.event.TelegramEvent;
import com.mario.services.telegram.storage.TelegramBotStorage;
import com.mario.services.telegram.storage.TelegramBotStorageConfig;
import com.mario.services.telegram.storage.impl.TelegramBotFileStorage;
import com.mario.services.telegram.storage.impl.TelegramBotMongoStorage;
import com.mongodb.MongoClient;
import com.nhb.common.Loggable;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventDispatcher;
import com.nhb.eventdriven.EventHandler;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

import lombok.Getter;
import lombok.Setter;

public class DefaultTelegramLongPollingBot extends TelegramLongPollingBot
		implements EventDispatcher, TelegramBot, Loggable {

	@Getter
	private final TelegramBotType type = TelegramBotType.LONG_POLLING;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String botToken;

	@Getter
	@Setter
	private String botUsername;

	private EventDispatcher eventDispatcher = new BaseEventDispatcher();

	private AtomicBoolean registered = new AtomicBoolean(false);

	@Setter
	@Getter
	private MarioApi api;

	private final TelegramBotStorageConfig storageConfig;

	private TelegramBotStorage storage;

	public DefaultTelegramLongPollingBot(String name, TelegramBotStorageConfig storageConfig) {
		this.setName(name);
		this.storageConfig = storageConfig;
	}

	public DefaultTelegramLongPollingBot(String name, TelegramBotStorageConfig storageConfig, String botToken,
			String botUsername) {
		this(name, storageConfig);
		this.setBotToken(botToken);
		this.setBotUsername(botUsername);
	}

	public DefaultTelegramLongPollingBot(String name, TelegramBotStorageConfig storageConfig, String botToken) {
		this(name, storageConfig);
		this.setBotToken(botToken);
	}

	@Override
	public void init() {
		switch (this.storageConfig.getType()) {
		case FILE:
			this.storage = new TelegramBotFileStorage(this.getBotUsername(), storageConfig.getFilePath());
			break;
		case MONGO:
			String dataSourceName = this.storageConfig.getDataSourceName();
			MongoClient mongoClient = this.getApi().getMongoClient(dataSourceName);
			if (mongoClient != null) {
				this.storage = new TelegramBotMongoStorage(this.botUsername, mongoClient);
			} else {
				throw new NullPointerException("Mongo datasource not exists for name " + dataSourceName);
			}
			break;
		default:
			throw new RuntimeException("Storage type not supported: " + storageConfig.getType());
		}
	}

	@Override
	public boolean isRegistered() {
		return this.registered.get();
	}

	@Override
	public boolean setRegistered() {
		return this.registered.compareAndSet(false, true);
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
	public void onUpdateReceived(Update update) {
		if (update.hasMessage()) {
			Long chatId = update.getMessage().getChatId();
			String phoneNumber = update.getMessage().getContact().getPhoneNumber();
			if (phoneNumber != null && chatId > 0) {
				try {
					this.storage.saveChatId(phoneNumber, chatId);
				} catch (Exception e) {
					getLogger().error("Cannot save chatId", e);
				}
			}
		}
		this.dispatchEvent(TelegramEvent.newUpdateEvent(update));
	}

	@Override
	public long getChatId(String phoneNumber) {
		return this.storage.getChatId(phoneNumber);
	}
}
