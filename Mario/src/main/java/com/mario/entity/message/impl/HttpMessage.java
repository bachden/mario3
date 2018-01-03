package com.mario.entity.message.impl;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.mario.config.gateway.GatewayType;
import com.mario.entity.message.CloneableMessage;
import com.mario.entity.message.Message;

public class HttpMessage extends BaseMessage implements CloneableMessage {

	private AsyncContext context;
	private ServletRequest request;
	private ServletResponse response;

	private int httpStatus = 200;

	public HttpMessage() {
		this.setGatewayType(GatewayType.HTTP);
	}

	public HttpSession getSession() {
		return this.request instanceof HttpServletRequest ? ((HttpServletRequest) this.request).getSession() : null;
	}

	public AsyncContext getContext() {
		return context;
	}

	public void setContext(AsyncContext context) {
		this.context = context;
	}

	public void clone(HttpMessage other) {
		this.context = other.context;
		this.request = other.request;
		this.response = other.response;
		this.setGatewayType(GatewayType.HTTP);
	}

	@Override
	public void clear() {
		this.context = null;
		this.request = null;
		this.response = null;
	}

	public ServletRequest getRequest() {
		if (this.request == null && this.context != null) {
			return this.context.getRequest();
		}
		return request;
	}

	public void setRequest(ServletRequest request) {
		this.request = request;
	}

	public ServletResponse getResponse() {
		if (this.response == null && this.context != null) {
			return this.context.getResponse();
		}
		return response;
	}

	public void setResponse(ServletResponse response) {
		this.response = response;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Message> T makeClone() {
		HttpMessage message = new HttpMessage();
		message.clone(this);
		return (T) message;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
}
