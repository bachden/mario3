package com.mario.services.telegram.bots;

import java.util.concurrent.atomic.AtomicBoolean;

import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.mario.api.MarioApi;
import com.mario.services.telegram.TelegramBot;
import com.mario.services.telegram.TelegramBotRegisterStrategy;
import com.mario.services.telegram.TelegramBotType;
import com.mario.services.telegram.event.TelegramEvent;
import com.mario.services.telegram.storage.TelegramBotStorage;
import com.mario.services.telegram.storage.TelegramBotStorageConfig;
import com.mario.services.telegram.storage.impl.TelegramBotFileStorage;
import com.mario.services.telegram.storage.impl.TelegramBotMongoStorage;
import com.mario.services.telegram.storage.impl.TelegramBotMySqlStorage;
import com.mongodb.MongoClient;
import com.nhb.common.Loggable;
import com.nhb.common.db.sql.DBIAdapter;
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

	private TelegramBotStorage storage;
	private final TelegramBotStorageConfig storageConfig;

	@Getter
	private final TelegramBotRegisterStrategy registerStrategy;

	@Setter
	@Getter
	private MarioApi api;

	private EventDispatcher eventDispatcher = new BaseEventDispatcher();

	public DefaultTelegramWebhookBot(String name, TelegramBotStorageConfig storageConfig,
			TelegramBotRegisterStrategy registerStrategy) {
		this.setName(name);
		this.storageConfig = storageConfig;
		this.registerStrategy = registerStrategy;
	}

	public DefaultTelegramWebhookBot(String name, TelegramBotStorageConfig storageConfig,
			TelegramBotRegisterStrategy registerStrategy, String botToken, String botUsername) {
		this(name, storageConfig, registerStrategy);
		this.setBotUsername(botUsername);
		this.setBotToken(botToken);
	}

	public DefaultTelegramWebhookBot(String name, TelegramBotStorageConfig storageConfig,
			TelegramBotRegisterStrategy registerStrategy, String botToken) {
		this(name, storageConfig, registerStrategy);
		this.setBotToken(botToken);
	}

	@Override
	public void init() {
		switch (this.storageConfig.getType()) {
		case FILE:
			this.storage = new TelegramBotFileStorage(this.getBotUsername(), storageConfig.getFilePath());
			break;
		case MONGO: {
			String dataSourceName = this.storageConfig.getDataSourceName();
			MongoClient mongoClient = this.getApi().getMongoClient(dataSourceName);
			if (mongoClient != null) {
				this.storage = new TelegramBotMongoStorage(this.botUsername, mongoClient);
			} else {
				throw new NullPointerException("Mongo datasource does not exists for name " + dataSourceName);
			}
			break;
		}
		case MYSQL: {
			String dataSourceName = this.storageConfig.getDataSourceName();
			DBIAdapter adapter = this.getApi().getDatabaseAdapter(dataSourceName);
			if (adapter != null) {
				this.storage = new TelegramBotMySqlStorage(this.botUsername, adapter);
			} else {
				throw new NullPointerException("MySQL datasource does not exists for name " + dataSourceName);
			}
			break;
		}
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
			Message message = update.getMessage();
			Long chatId = message.getChatId();
			String userName = message.getFrom().getUserName();

			SendMessage reply = new SendMessage();
			reply.setChatId(chatId);
			if (userName != null) {
				if (getChatId(userName) > 0) {
					reply.setText("Hello, you're already registered, now you just have to wait for alert");
				} else {
					try {
						if (this.storage.saveChatId(userName, chatId)) {
							getLogger().info("TelegramBot name {} --> saved userName {} and chat id {}",
									this.botUsername, userName, chatId);
							reply.setText("Your userName is '" + userName
									+ "', I did save and will re-use it to send alert to you");
						} else {
							reply.setText("Your userName is '" + userName
									+ "', I can't save it because of unknown error, I'm sorry, try again later");
						}
					} catch (Exception e) {
						getLogger().error("Cannot save chatId", e);
						reply.setText("Your userName is '{}', I can't save it because of '" + e.getMessage()
								+ "', I'm sorry, try again later");
					}
				}
			} else {
				reply.setText("Your userName doesn't set, please do it in profile setting and chat with me again");
			}

			try {
				this.sendMessage(reply);
			} catch (TelegramApiException e) {
				getLogger().error("Cannot send reply message", e);
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
