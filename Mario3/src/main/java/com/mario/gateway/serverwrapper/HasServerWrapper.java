package com.mario.gateway.serverwrapper;

public interface HasServerWrapper<ServerWrapperType extends ServerWrapper> {

	void setServer(ServerWrapperType server);
}
