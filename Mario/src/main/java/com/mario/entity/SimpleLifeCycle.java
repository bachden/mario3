package com.mario.entity;

import com.nhb.common.data.PuObjectRO;

public interface SimpleLifeCycle {

	public void init(PuObjectRO initParams);

	public void destroy() throws Exception;
}
