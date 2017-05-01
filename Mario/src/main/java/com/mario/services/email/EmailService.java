package com.mario.services.email;

import com.mario.entity.NamedLifeCycle;
import com.mario.entity.SimpleLifeCycle;

public interface EmailService extends SimpleLifeCycle, NamedLifeCycle {

	void send(EmailEnvelope envelope);
}
