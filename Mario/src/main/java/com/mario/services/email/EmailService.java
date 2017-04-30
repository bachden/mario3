package com.mario.services.email;

import com.mario.entity.SimpleLifeCycle;

public interface EmailService extends SimpleLifeCycle {

	String getName();

	void send(EmailEnvelope envelope);
}
