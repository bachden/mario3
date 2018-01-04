package com.mario.services.email.config;

import com.nhb.common.data.PuObjectRO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class EmailServiceConfig {

	private String name;

	private String extensionName;

	private String handle;

	private PuObjectRO initParams;

	private IncomingMailServerConfig incomingConfig;

	private OutgoingMailServerConfig outgoingConfig;
}
