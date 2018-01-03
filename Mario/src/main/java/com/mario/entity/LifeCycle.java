package com.mario.entity;

import com.nhb.common.Loggable;

public interface LifeCycle extends SimpleLifeCycle, NamedLifeCycle, ExtensionElement, Loggable {

	public void onExtensionReady();

	public void onServerReady();

}
