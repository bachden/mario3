package com.mario.config.gateway;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import com.mario.entity.message.transcoder.http.DefaultHttpMessageDeserializer;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HttpGatewayConfig extends GatewayConfig {

	private String path = "/*";
	private String encoding = "utf-8";
	private String contentType = null; // "text/plain";
	private boolean useMultipath = false;
	private boolean async = false;

	private final Map<String, String> headers = new HashMap<>();

	{
		this.setType(GatewayType.HTTP);
		this.setDeserializerClassName(DefaultHttpMessageDeserializer.class.getName());
		// this.setSerializerClassName(DefaultHttpMessageSerializer.class.getName());
	}

	@Override
	protected void _readPuObject(PuObjectRO data) {
		super._readPuObject(data);
		if (data.variableExists("path")) {
			this.setPath(data.getString("path"));
		}
		if (data.variableExists("encoding")) {
			this.setEncoding(data.getString("encoding"));
		}
		if (data.variableExists("contentType")) {
			this.setContentType(data.getString("contenttype"));
		}
		if (data.variableExists("useMultipart")) {
			this.setUseMultipath(data.getBoolean("useMultipart"));
		} else if (data.variableExists("usingMultipart")) {
			this.setUseMultipath(data.getBoolean("usingMultipart"));
		}
		if (data.variableExists("async")) {
			this.setAsync(data.getBoolean("async"));
		}
	}

	@Override
	public void readNode(Node item) {
		Node ele = item.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == 1) {
				String value = ele.getTextContent().trim();
				String nodeName = ele.getNodeName();
				if (nodeName.equalsIgnoreCase("deserializer")) {
					this.setDeserializerClassName(value);
				} else if (nodeName.equalsIgnoreCase("serializer")) {
					this.setSerializerClassName(value);
				} else if (nodeName.equalsIgnoreCase("name")) {
					this.setName(value);
				} else if (nodeName.equalsIgnoreCase("workerpool")) {
					this.setWorkerPoolConfig(readWorkerPoolConfig(ele));
				} else if (nodeName.equalsIgnoreCase("path") || nodeName.equalsIgnoreCase("location")) {
					this.setPath(value);
				} else if (nodeName.equalsIgnoreCase("async")) {
					this.setAsync(Boolean.valueOf(value));
				} else if (nodeName.equalsIgnoreCase("encoding")) {
					this.setEncoding(value);
				} else if (nodeName.equalsIgnoreCase("server")) {
					this.setServerWrapperName(value);
				} else if (nodeName.equalsIgnoreCase("usemultipart") || nodeName.equalsIgnoreCase("usingmultipart")) {
					this.setUseMultipath(Boolean.valueOf(value));
				} else if (nodeName.equalsIgnoreCase("header")) {
					String key = ele.getAttributes().getNamedItem("name").getNodeValue();
					if (key != null && key.trim().length() > 0) {
						this.getHeaders().put(key.trim(), value);
					}
				}
			}
			ele = ele.getNextSibling();
		}
	}
}
