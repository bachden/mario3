package com.mario.gateway.zeromq.metadata;

import com.mario.gateway.zeromq.ZeroMQMessage;
import com.nhb.common.data.PuArray;

public interface ZeroMQInputMetadataProcessor {

	void processInputMetadata(PuArray metadata, ZeroMQMessage message);
}
