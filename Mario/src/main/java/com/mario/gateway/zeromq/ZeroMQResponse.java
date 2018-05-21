package com.mario.gateway.zeromq;

import com.lmax.disruptor.EventFactory;
import com.mario.gateway.zeromq.metadata.ZeroMQOutputMetadataProcessor;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuElement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ZeroMQResponse {

	public static final EventFactory<ZeroMQResponse> EVENT_FACTORY = new EventFactory<ZeroMQResponse>() {

		@Override
		public ZeroMQResponse newInstance() {
			return new ZeroMQResponse();
		}
	};

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	private byte[] rawOutput;

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	private PuElement output;

	@Getter(AccessLevel.PACKAGE)
	private byte[] messageId;

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	private String responseEndpoint;

	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	private ZeroMQOutputMetadataProcessor metadataProcessor;

	public void fillData(ZeroMQMessage source) {
		this.messageId = source.getMessageId();
		this.responseEndpoint = source.getResponseEndpoint();
	}

	public PuArray getMetadata() {
		return this.getMetadataProcessor() == null ? null : this.getMetadataProcessor().createOutputMetadata(this);
	}
}
