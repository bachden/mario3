package com.mario.services.telegram.storage.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import com.mario.services.telegram.storage.TelegramBotStorage;
import com.nhb.common.Loggable;
import com.nhb.common.db.sql.DBIAdapter;
import com.nhb.common.db.sql.daos.BaseMySqlDAO;

public class TelegramBotMySqlStorage implements TelegramBotStorage, Loggable {

	public static final String TABLE_NAME = "chat_ids";

	private final String botUsername;
	private final DBIAdapter adapter;

	public static abstract class ChatIdDAO extends BaseMySqlDAO {

		@SqlQuery("SELECT chat_id FROM chat_ids WHERE phone_number=:phoneNumber AND bot_username=:botUsername")
		public abstract Long fetchChatIdByPhoneNumberAndBotUsername(@Bind("phoneNumber") String phoneNumber,
				@Bind("botUsername") String botUsername);

		@SqlUpdate("INSERT INTO `chat_ids` (`bot_username`, `phone_number`, `chat_id`) VALUES (:botUsername, :phoneNumber, :chatId)")
		public abstract void insert(@Bind("phoneNumber") String phoneNumber, @Bind("botUsername") String botUsername,
				@Bind("chatId") long chatId);

		@SqlQuery("SHOW TABLES LIKE :tableName")
		public abstract List<String> showTable(@Bind("tableName") String tableName);

		@SqlUpdate("CREATE TABLE `chat_ids` (`id` INT NOT NULL AUTO_INCREMENT, `bot_username` VARCHAR(128) NOT NULL, `phone_number` VARCHAR(15) NOT NULL, `chat_id` BIGINT(20) NOT NULL, PRIMARY KEY (`id`), UNIQUE INDEX `botusername_phonenumber_unq` (`bot_username` ASC, `phone_number` ASC))")
		public abstract void createTable();
	}

	private final Map<String, Long> localCache = new ConcurrentHashMap<>();

	public TelegramBotMySqlStorage(String botUsername, DBIAdapter dbiAdapter) {
		this.botUsername = botUsername;
		this.adapter = dbiAdapter;

		this.init();
	}

	private void init() {
		try (ChatIdDAO dao = this.adapter.openDAO(ChatIdDAO.class)) {
			List<String> tableNames = dao.showTable(TABLE_NAME);
			if (tableNames.size() == 0) {
				dao.createTable();
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
					try (ChatIdDAO dao = this.adapter.openDAO(ChatIdDAO.class)) {
						dao.insert(phoneNumber, botUsername, chatId);
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
	public long getChatId(String phoneNumber) {
		if (!this.localCache.containsKey(phoneNumber)) {
			synchronized (getMonitorObject(phoneNumber)) {
				if (!this.localCache.containsKey(phoneNumber)) {
					try (ChatIdDAO dao = this.adapter.openDAO(ChatIdDAO.class)) {
						Long chatId = dao.fetchChatIdByPhoneNumberAndBotUsername(phoneNumber, this.botUsername);
						if (chatId != null) {
							this.localCache.put(phoneNumber, chatId);
						} else {
							return -1;
						}
					}
				}
			}
		}
		return this.localCache.get(phoneNumber);
	}

}
