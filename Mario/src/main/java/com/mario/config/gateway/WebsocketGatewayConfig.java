package com.mario.config.gateway;

import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebsocketGatewayConfig extends SocketGatewayConfig {

	private String path = "/websocket";
	private String proxy = null;
	private boolean autoActiveChannel = true;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		super._readPuObject(data);
		if (data.variableExists("path")) {
			this.setPath(data.getString("path"));
		}
		if (data.variableExists("proxy")) {
			this.setProxy(data.getString("proxy"));
		}
		if (data.variableExists("autoActiveChannel")) {
			this.setAutoActiveChannel(data.getBoolean("autoActiveChannel"));
		}
	}

}
