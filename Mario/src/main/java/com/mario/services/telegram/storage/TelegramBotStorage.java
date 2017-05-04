package com.mario.services.telegram.storage;

public interface TelegramBotStorage {

	boolean saveChatId(String phoneNumber, long chatId);

	long getChatId(String phoneNumber);
}
