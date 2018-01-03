package com.mario.entity;

import com.nhb.common.Loggable;
import com.nhb.common.data.PuObject;

public interface ManagedObject extends LifeCycle, Pluggable, Loggable {

	Object acquire(PuObject requestParams);

	void release(Object object);
}
