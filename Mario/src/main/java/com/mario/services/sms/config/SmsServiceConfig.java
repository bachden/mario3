package com.mario.services.sms.config;

import com.nhb.common.data.PuObjectRO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class SmsServiceConfig {

	private String name;
	private String extensionName;
	private String handle;
	private PuObjectRO initParams;
}
