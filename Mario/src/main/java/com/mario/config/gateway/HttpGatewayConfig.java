package com.mario.config.gateway;

import java.util.HashMap;
import java.util.Map;

import com.mario.entity.message.transcoder.http.DefaultHttpMessageDeserializer;
import com.nhb.common.data.PuObjectRO;

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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public boolean isUseMultipath() {
		return useMultipath;
	}

	public void setUseMultipath(boolean useMultipath) {
		this.useMultipath = useMultipath;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}
}
