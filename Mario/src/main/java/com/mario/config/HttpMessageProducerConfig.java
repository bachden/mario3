package com.mario.config;

import org.w3c.dom.Node;

import com.mario.config.gateway.GatewayType;
import com.nhb.common.data.PuObjectRO;
import com.nhb.messaging.http.HttpMethod;

public class HttpMessageProducerConfig extends MessageProducerConfig {

	{
		setGatewayType(GatewayType.HTTP);
	}

	private String endpoint;
	private boolean async = false;
	private boolean usingMultipart = true;
	private HttpMethod httpMethod = HttpMethod.GET;

	@Override
	public void readNode(Node node) {
		Node ele = node.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == 1) {
				String nodeName = ele.getNodeName();
				String value = ele.getTextContent().trim();
				switch (nodeName.toLowerCase()) {
				case "name":
					this.setName(value);
					break;
				case "endpoint":
					this.setEndpoint(value);
					break;
				case "method":
					this.setHttpMethod(HttpMethod.fromName(value));
					break;
				case "async":
					this.setAsync(Boolean.valueOf(value));
					break;
				case "usemultipart":
				case "usingmultipart":
					this.setUsingMultipart(Boolean.valueOf(value));
					break;
				}
			}
			ele = ele.getNextSibling();
		}
	}

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("endpoint")) {
			this.setEndpoint(data.getString("endpoint"));
		}
		if (data.variableExists("async")) {
			this.setAsync(data.getBoolean("async"));
		}
		if (data.variableExists("usingMultipart")) {
			this.setUsingMultipart(data.getBoolean("usingMultipart"));
		}
		if (data.variableExists("method")) {
			this.setHttpMethod(HttpMethod.fromName(data.getString("method")));
		}
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public boolean isUsingMultipart() {
		return usingMultipart;
	}

	public void setUsingMultipart(boolean usingMultipart) {
		this.usingMultipart = usingMultipart;
	}

}
