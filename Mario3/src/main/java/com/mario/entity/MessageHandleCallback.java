package com.mario.entity;

import com.mario.entity.message.Message;
import com.nhb.common.data.PuElement;

public interface MessageHandleCallback {

	void onHandleComplete(Message message, PuElement result);

	void onHandleError(Message message, Throwable exception);
}
