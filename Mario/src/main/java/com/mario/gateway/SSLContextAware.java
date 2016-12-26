package com.mario.gateway;

import javax.net.ssl.SSLContext;

public interface SSLContextAware {

	void setSSLContext(SSLContext sslContext);
}
