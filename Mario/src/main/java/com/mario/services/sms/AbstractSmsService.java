package com.mario.services.sms;

import com.mario.entity.NamedLifeCycle;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractSmsService implements SmsService, NamedLifeCycle, Loggable {

	@Setter
	@Getter
	private String name;

	@Override
	public void init(PuObjectRO initParams) {
		// do nothing
	}

	@Override
	public void destroy() throws Exception {
		// do nothing
	}

}
