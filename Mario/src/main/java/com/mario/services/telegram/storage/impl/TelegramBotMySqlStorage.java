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

		@SqlQuery("SELECT chat_id FROM chat_ids WHERE username=:userName AND bot_username=:botUsername")
		public abstract Long fetchChatIdByUserNameAndBotUsername(@Bind("userName") String userName,
				@Bind("botUsername") String botUsername);

		@SqlUpdate("INSERT INTO `chat_ids` (`bot_username`, `username`, `chat_id`) VALUES (:botUsername, :userName, :chatId)")
		public abstract void insert(@Bind("userName") String userName, @Bind("botUsername") String botUsername,
				@Bind("chatId") long chatId);

		@SqlQuery("SHOW TABLES LIKE :tableName")
		public abstract List<String> showTable(@Bind("tableName") String tableName);

		@SqlUpdate("CREATE TABLE `chat_ids` (`id` INT NOT NULL AUTO_INCREMENT, `bot_username` VARCHAR(128) NOT NULL, `username` VARCHAR(15) NOT NULL, `chat_id` BIGINT(20) NOT NULL, PRIMARY KEY (`id`), UNIQUE INDEX `botusername_username_unq` (`bot_username` ASC, `username` ASC))")
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

	private Object getMonitorObject(String userName) {
		Object obj = new Object();
		Object old = this.synchronizedMonitorObjects.putIfAbsent(userName, obj);
		return old != null ? old : obj;
	}

	@Override
	public boolean saveChatId(String userName, long chatId) {
		if (this.getChatId(userName) == -1) {
			synchronized (getMonitorObject(userName)) {
				if (this.getChatId(userName) == -1) {
					this.localCache.put(userName, chatId);
					try (ChatIdDAO dao = this.adapter.openDAO(ChatIdDAO.class)) {
						dao.insert(userName, botUsername, chatId);
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
					try (ChatIdDAO dao = this.adapter.openDAO(ChatIdDAO.class)) {
						Long chatId = dao.fetchChatIdByUserNameAndBotUsername(userName, this.botUsername);
						if (chatId != null) {
							this.localCache.put(userName, chatId);
						} else {
							return -1;
						}
					}
				}
			}
		}
		return this.localCache.get(userName);
	}

}
