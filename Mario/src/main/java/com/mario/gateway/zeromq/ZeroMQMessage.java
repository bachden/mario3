package com.mario.gateway.zeromq;

import com.mario.config.gateway.GatewayType;
import com.mario.entity.MessageHandleCallback;
import com.mario.entity.message.CloneableMessage;
import com.mario.entity.message.Message;
import com.nhb.common.async.CompletableFuture;
import com.nhb.common.data.PuElement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ZeroMQMessage implements Message, CloneableMessage {

	@Getter
	@Setter(AccessLevel.PACKAGE)
	private PuElement data;

	@Getter
	@Setter(AccessLevel.PACKAGE)
	private String gatewayName;

	@Getter
	@Setter(AccessLevel.PACKAGE)
	private GatewayType gatewayType;

	@Getter
	@Setter(AccessLevel.PACKAGE)
	private MessageHandleCallback callback;

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private CompletableFuture<PuElement> future;

	@Override
	public <T extends Message> T makeClone() {
		ZeroMQMessage cloneMessage = new ZeroMQMessage();
		cloneMessage.setData(this.getData());
		cloneMessage.setFuture(this.getFuture());
		cloneMessage.setCallback(this.getCallback());
		cloneMessage.setGatewayName(this.getGatewayName());
		cloneMessage.setGatewayType(this.getGatewayType());
		return cloneMessage.cast();
	}
}
