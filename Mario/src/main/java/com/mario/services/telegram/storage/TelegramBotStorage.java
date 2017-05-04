package com.mario.services.telegram.storage;

public interface TelegramBotStorage {

	void saveChatId(String phoneNumber, long chatId);

	long getChatId(String phoneNumber);
}
