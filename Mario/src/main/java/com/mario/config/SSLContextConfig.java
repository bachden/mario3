package com.mario.config;

import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SSLContextConfig extends MarioBaseConfig {

	private String format = "JKS";
	private String protocol = "TLS";
	private String algorithm = "RSA";

	private String filePath;
	private String password;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data != null) {
			this.format = data.getString("format", this.format);
			this.protocol = data.getString("protocol", this.protocol);
			this.algorithm = data.getString("algorithm", this.algorithm);

			this.filePath = data.getString("filePath", null);
			this.password = data.getString("password", null);
		}
	}
}
