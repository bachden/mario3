package com.mario.entity;

import com.mario.api.MarioApi;

public interface Pluggable {

	void setApi(MarioApi api);

	MarioApi getApi();
}
