package com.mario.services.sms;

import java.util.Arrays;
import java.util.Collection;

import com.mario.entity.SimpleLifeCycle;

public interface SmsService extends SimpleLifeCycle {

	String getName();

	void send(String message, Collection<String> recipients);

	default void send(String message, String... recipients) {
		if (recipients != null && recipients.length > 0) {
			this.send(message, Arrays.asList(recipients));
		} else {
			throw new NullPointerException("Recipients cannot be empty (or null)");
		}
	}
}
