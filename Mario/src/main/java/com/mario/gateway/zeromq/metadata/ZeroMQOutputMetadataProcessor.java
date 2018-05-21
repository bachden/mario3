package com.mario.gateway.zeromq.metadata;

import com.mario.gateway.zeromq.ZeroMQResponse;
import com.nhb.common.data.PuArray;

public interface ZeroMQOutputMetadataProcessor {

	PuArray createOutputMetadata(ZeroMQResponse message);
}
