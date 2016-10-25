package nhb.mario3.entity.message.impl;

import nhb.common.data.PuElement;
import nhb.mario3.config.gateway.GatewayType;
import nhb.mario3.entity.MessageHandleCallback;
import nhb.mario3.entity.message.DecodingErrorMessage;
import nhb.mario3.entity.message.MessageForwardable;
import nhb.mario3.entity.message.transcoder.MessageDecodingException;
import nhb.messaging.MessageForwarder;

public class BaseMessage implements MessageForwardable, DecodingErrorMessage {

	private PuElement data;
	private String gatewayName;
	private GatewayType gatewayType;
	private MessageForwarder forwarder;
	private MessageHandleCallback callback;
	private MessageDecodingException messageDecodingException;

	@Override
	public void clear() {
		this.data = null;
		this.messageDecodingException = null;
	}

	@Override
	public PuElement getData() {
		return data;
	}

	@Override
	public void setData(PuElement data) {
		this.data = data;
	}

	@Override
	public MessageForwarder getForwarder() {
		return forwarder;
	}

	@Override
	public void setForwarder(MessageForwarder forwarder) {
		this.forwarder = forwarder;
	}

	@Override
	public String getGatewayName() {
		return gatewayName;
	}

	@Override
	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	@Override
	public GatewayType getGatewayType() {
		return gatewayType;
	}

	@Override
	public void setGatewayType(GatewayType gatewayType) {
		this.gatewayType = gatewayType;
	}

	public MessageHandleCallback getCallback() {
		return callback;
	}

	public void setCallback(MessageHandleCallback callback) {
		this.callback = callback;
	}

	@Override
	public void setDecodingFailedCause(MessageDecodingException ex) {
		this.messageDecodingException = ex;
	}

	@Override
	public MessageDecodingException getDecodingFailedCause() {
		return this.messageDecodingException;
	}

	protected void fillProperties(BaseMessage other) {
		if (other != null) {
			other.callback = this.callback;
			other.data = this.data;
			other.forwarder = this.forwarder;
			other.gatewayName = this.gatewayName;
			other.gatewayType = this.gatewayType;
			other.messageDecodingException = this.messageDecodingException;
		}
	}
}
