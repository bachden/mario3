package com.mario.services.sms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nhb.common.Loggable;

public class SmsServiceManager implements Loggable {

	private final Map<String, SmsService> smsServices = new ConcurrentHashMap<>();

	public SmsService getSmsService(String name) {
		return this.smsServices.get(name);
	}

	public void register(SmsService service) {
		this.smsServices.put(service.getName(), service);
	}

	public void shutdown() {
		for (SmsService smsService : this.smsServices.values()) {
			try {
				smsService.destroy();
			} catch (Exception e) {
				e.printStackTrace();
				getLogger().error("Error while destroy smsService name " + smsService);
			}
		}
	}

}
