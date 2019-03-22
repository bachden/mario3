package com.mario.services.telegram.bots;

import java.util.concurrent.atomic.AtomicBoolean;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

	@Getter
	private final TelegramBotRegisterStrategy registerStrategy;

	@Setter
	@Getter
	private MarioApi api;

	private final TelegramBotStorageConfig storageConfig;

	@Setter
	private boolean sendAckEvenRegistered = false;

	@Setter
	private boolean autoSendAck = false;

	private TelegramBotStorage storage;

	public DefaultTelegramLongPollingBot(String name, TelegramBotStorageConfig storageConfig,
			TelegramBotRegisterStrategy registerStrategy) {
		this.setName(name);
		this.storageConfig = storageConfig;
		this.registerStrategy = registerStrategy;
	}

	public DefaultTelegramLongPollingBot(String name, TelegramBotStorageConfig storageConfig,
			TelegramBotRegisterStrategy registerStrategy, String botToken, String botUsername) {
		this(name, storageConfig, registerStrategy);
		this.setBotToken(botToken);
		this.setBotUsername(botUsername);
	}

	public DefaultTelegramLongPollingBot(String name, TelegramBotStorageConfig storageConfig,
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
		TelegramEvent event;
		if (update.hasMessage()) {
			Message message = update.getMessage();
			Long chatId = message.getChatId();
			String userName = message.getFrom().getUserName();

			event = TelegramEvent.newUpdateEvent(chatId, userName, update);

			SendMessage reply = new SendMessage();
			reply.setChatId(chatId);

			if (userName != null) {
				if (getChatId(userName) > 0) {
					if (sendAckEvenRegistered) {
						reply.setText("Hello, you're already registered, now you just have to wait for alert");
					}
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

			if (autoSendAck) {
				try {
					this.execute(reply);
				} catch (TelegramApiException e) {
					getLogger().error("Cannot send reply message", e);
				}
			}
		} else {
			event = TelegramEvent.newUpdateEvent(-1, null, update);
		}
		this.dispatchEvent(event);
	}

	@Override
	public long getChatId(String userName) {
		return this.storage.getChatId(userName);
	}
}
