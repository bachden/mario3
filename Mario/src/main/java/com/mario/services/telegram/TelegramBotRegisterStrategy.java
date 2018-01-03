package com.mario.services.telegram;

public enum TelegramBotRegisterStrategy {

	LAZY, IMMEDIATELY;

	public static TelegramBotRegisterStrategy fromName(String name) {
		if (name != null) {
			for (TelegramBotRegisterStrategy value : values()) {
				if (value.name().equalsIgnoreCase(name)) {
					return value;
				}
			}
		}
		return null;
	}
}
