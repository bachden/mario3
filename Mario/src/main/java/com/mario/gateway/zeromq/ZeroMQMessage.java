package com.mario.gateway.zeromq;

import com.lmax.disruptor.EventFactory;
import com.mario.config.gateway.GatewayType;
import com.mario.entity.message.CloneableMessage;
import com.mario.entity.message.impl.BaseMessage;
import com.mario.gateway.zeromq.metadata.ZeroMQInputMetadataProcessor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ZeroMQMessage extends BaseMessage implements CloneableMessage {

	protected static EventFactory<ZeroMQMessage> EVENT_FACTORY = new EventFactory<ZeroMQMessage>() {

		@Override
		public ZeroMQMessage newInstance() {
			return new ZeroMQMessage();
		}
	};

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	private byte[] rawInput;

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	private String responseEndpoint;

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	private byte[] messageId;

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	private ZeroMQInputMetadataProcessor metadataProcessor;

	public ZeroMQMessage() {
		this.setGatewayType(GatewayType.ZEROMQ);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ZeroMQMessage makeClone() {
		ZeroMQMessage result = new ZeroMQMessage();
		this.fillProperties(result);
		result.messageId = this.messageId;
		result.rawInput = this.rawInput;
		result.responseEndpoint = this.responseEndpoint;
		result.metadataProcessor = this.metadataProcessor;
		return result;
	}
}
