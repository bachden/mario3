package com.mario.worker;

import com.lmax.disruptor.EventFactory;
import com.mario.entity.message.Message;
import com.mario.entity.message.impl.BaseMessage;

public interface MessageEventFactory extends EventFactory<Message> {

	@Override
	default Message newInstance() {
		BaseMessage result = new BaseMessage();
		return result;
	}

}
