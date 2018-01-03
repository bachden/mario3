package com.mario.test.http;

import org.apache.http.HttpResponse;

import nhb.common.async.Callback;
import nhb.common.data.PuElement;
import nhb.common.data.PuNull;
import nhb.mario3.entity.MessageHandleCallback;
import nhb.mario3.entity.impl.BaseMessageHandler;
import nhb.mario3.entity.message.CloneableMessage;
import nhb.mario3.entity.message.Message;
import nhb.messaging.http.HttpClientHelper;
import nhb.messaging.http.producer.HttpAsyncMessageProducer;

public class TestHttpGatewayHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message message) {
		// return new PuValue(message.getData().toJSON());
		HttpAsyncMessageProducer forwarder = getApi().getProducer("maxpay");

		MessageHandleCallback messageHandleCallback = message.getCallback();

		// HttpMessage httpMessage = (HttpMessage) message;
		// HttpServletRequest servletRequest = (HttpServletRequest)
		// httpMessage.getRequest();

		final Message cloneMessage = (message instanceof CloneableMessage) ? ((CloneableMessage) message).makeClone()
				: null;
		forwarder.publish(message.getData()).setCallback(new Callback<HttpResponse>() {

			@Override
			public void apply(HttpResponse result) {
				if (messageHandleCallback != null) {
					messageHandleCallback.onHandleComplete(cloneMessage, HttpClientHelper.handleResponse(result));
				}
			}
		});

		return PuNull.IGNORE_ME;
	}
}
