package com.mario.services.telegram;

public enum TelegramBotType {

	WEBHOOK, LONG_POLLING;

	public static TelegramBotType fromName(String name) {
		if (name != null) {
			name = name.trim();
			for (TelegramBotType type : values()) {
				if (type.name().equalsIgnoreCase(name)) {
					return type;
				}
			}
		}
		return null;
	}
}
