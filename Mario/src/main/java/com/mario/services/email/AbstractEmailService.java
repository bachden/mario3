package com.mario.services.email;

import com.mario.services.email.config.IncomingMailServerConfig;
import com.mario.services.email.config.OutgoingMailServerConfig;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuObjectRO;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractEmailService implements EmailService, EmailServiceConfigurable, Loggable {

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private IncomingMailServerConfig incomingConfig;

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private OutgoingMailServerConfig outgoingConfig;

	@Setter
	@Getter
	private String name;

	@Override
	public void init(PuObjectRO initParams) {

	}

	@Override
	public void destroy() throws Exception {

	}

}
