package com.mario.services.telegram;

import com.mario.entity.Pluggable;

public interface TelegramBot extends Pluggable {

	String getName();

	TelegramBotType getType();

	String getBotUsername();

	String getBotToken();

	void init();

	/**
	 * check if this bot is registered with TelegramApi
	 * 
	 * @return
	 */
	boolean isRegistered();

	/**
	 * set this bot as registered with TelegramApi
	 * 
	 * @return true if this bot did not registered before
	 */
	boolean setRegistered();
	
	TelegramBotRegisterStrategy getRegisterStrategy();

	long getChatId(String phoneNumber);
}
