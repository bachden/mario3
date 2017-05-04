package com.mario.services.telegram.storage;

public interface TelegramBotStorage {

	boolean saveChatId(String userName, long chatId);

	long getChatId(String userName);
}
