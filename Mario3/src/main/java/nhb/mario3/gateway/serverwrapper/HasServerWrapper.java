package nhb.mario3.gateway.serverwrapper;

public interface HasServerWrapper<ServerWrapperType extends ServerWrapper> {

	void setServer(ServerWrapperType server);
}
