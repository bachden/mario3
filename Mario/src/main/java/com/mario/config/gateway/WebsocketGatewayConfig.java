package com.mario.config.gateway;

import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

public class WebsocketGatewayConfig extends SocketGatewayConfig {

	@Setter
	@Getter
	private String path = "";
	@Setter
	@Getter
	private String proxy = null;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		super._readPuObject(data);
		if (data.variableExists("path")) {
			this.setPath(data.getString("path"));
		}
	}

}
