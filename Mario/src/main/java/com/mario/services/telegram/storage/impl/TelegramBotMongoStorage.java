package com.mario.services.telegram.storage.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.mario.services.telegram.storage.TelegramBotStorage;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.nhb.common.Loggable;

public class TelegramBotMongoStorage implements TelegramBotStorage, Loggable {

	private static final String CHAT_ID = "chatId";

	private static final String BOT_USERNAME = "botUsername";

	private static final String USERNAME = "userName";

	public static final String BOT_CHAT_IDS_COLLECTION_NAME = "chatIds";

	private MongoCollection<Document> botChatIdCollection;

	private String databaseName = "bot_chat_ids";

	private final String botUsername;

	private final MongoClient mongoClient;

	private final Map<String, Long> localCache = new ConcurrentHashMap<>();

	public TelegramBotMongoStorage(String botUsername, MongoClient mongoClient) {
		this.botUsername = botUsername;
		this.mongoClient = mongoClient;
		this.init();
	}

	protected MongoCollection<Document> getBotChatIdCollection() {
		if (this.botChatIdCollection == null) {
			synchronized (this) {
				if (this.botChatIdCollection == null) {
					this.botChatIdCollection = getDatabase().getCollection(BOT_CHAT_IDS_COLLECTION_NAME);
				}
			}
		}
		return this.botChatIdCollection;
	}

	private MongoDatabase getDatabase() {
		return this.mongoClient.getDatabase(this.databaseName);
	}

	private void init() {
		MongoCollection<Document> collection = this.getBotChatIdCollection();
		List<Document> indexes = Arrays.asList(new Document().append(USERNAME, 1).append(BOT_USERNAME, 1));
		for (Document index : indexes) {
			try {
				collection.createIndex(index, new IndexOptions().unique(true));
			} catch (Exception e) {
				getLogger().warn("Create index error", e);
			}
		}
	}

	private final Map<String, Object> synchronizedMonitorObjects = new ConcurrentHashMap<>();

	private Object getMonitorObject(String phoneNumber) {
		Object obj = new Object();
		Object old = this.synchronizedMonitorObjects.putIfAbsent(phoneNumber, obj);
		return old != null ? old : obj;
	}

	@Override
	public boolean saveChatId(String phoneNumber, long chatId) {
		if (this.getChatId(phoneNumber) == -1) {
			synchronized (getMonitorObject(phoneNumber)) {
				if (this.getChatId(phoneNumber) == -1) {
					this.localCache.put(phoneNumber, chatId);
					try {
						this.getBotChatIdCollection().insertOne(new Document().append(USERNAME, phoneNumber)
								.append(BOT_USERNAME, this.botUsername).append(CHAT_ID, chatId));
						getLogger().debug("Saved phoneNumber {} and chatId {} for bot {}", phoneNumber, chatId,
								botUsername);
						return true;
					} catch (Exception e) {
						throw e;
					}
				}
			}
		}
		return false;
	}

	@Override
	public long getChatId(String userName) {
		if (!this.localCache.containsKey(userName)) {
			synchronized (getMonitorObject(userName)) {
				if (!this.localCache.containsKey(userName)) {
					FindIterable<Document> found = this.getBotChatIdCollection()
							.find(new Document().append(BOT_USERNAME, this.botUsername).append(USERNAME, userName));
					Document first = found.first();
					if (first != null) {
						this.localCache.put(userName, first.getLong(CHAT_ID));
					} else {
						return -1;
					}
				}
			}
		}
		return this.localCache.get(userName);
	}

}
