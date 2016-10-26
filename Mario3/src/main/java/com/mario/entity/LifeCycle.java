package com.mario.entity;

import com.nhb.common.Loggable;
import com.nhb.common.data.PuObjectRO;

public interface LifeCycle extends Loggable {

	public String getExtensionName();

	public void setExtensionName(String name);

	public String getName();

	public void setName(String name);

	public void init(PuObjectRO initParams);

	public void onExtensionReady();
	
	public void onServerReady();
	
	public void destroy() throws Exception;
}
