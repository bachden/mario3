package com.mario.entity.message;

public interface CloneableMessage {

	<T extends Message> T makeClone();
}
