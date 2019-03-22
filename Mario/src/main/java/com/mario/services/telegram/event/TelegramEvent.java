package com.mario.services.telegram.event;

import org.telegram.telegrambots.meta.api.objects.Update;

import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.impl.AbstractEvent;

import lombok.Getter;

@Getter
public class TelegramEvent extends AbstractEvent implements Event {

	public static final String UPDATE = "update";

	private Update update;
	private long chatId;
	private String userName;

	public static TelegramEvent newUpdateEvent(long chatId, String userName, Update update) {
		TelegramEvent event = new TelegramEvent();
		event.setType(UPDATE);
		event.update = update;
		event.chatId = chatId;
		event.userName = userName;
		return event;
	}
}
