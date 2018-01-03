package com.mario.services.telegram.event;

import org.telegram.telegrambots.api.objects.Update;

import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.impl.AbstractEvent;

import lombok.Getter;

public class TelegramEvent extends AbstractEvent implements Event {

	public static final String UPDATE = "update";

	@Getter
	private Update update;

	public static TelegramEvent newUpdateEvent(Update update) {
		TelegramEvent event = new TelegramEvent();
		event.setType(UPDATE);
		event.update = update;
		return event;
	}
}
