package com.mario.services.email;

import com.mario.services.email.config.IncomingMailServerConfig;
import com.mario.services.email.config.OutgoingMailServerConfig;

public interface EmailServiceConfigurable {

	void setIncomingConfig(IncomingMailServerConfig incomingConfig);

	void setOutgoingConfig(OutgoingMailServerConfig outgoingConfig);
}
