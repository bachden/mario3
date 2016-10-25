package nhb.mario3.config.serverwrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import nhb.common.data.PuObjectRO;
import nhb.common.vo.HostAndPort;
import nhb.common.vo.UserNameAndPassword;

public class RabbitMQServerWrapperConfig extends ServerWrapperConfig {

	private Collection<HostAndPort> endpoints;
	private UserNameAndPassword credential;

	{
		this.setType(ServerWrapperType.RABBITMQ);
	}

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("endpoints")) {
			if (this.endpoints == null) {
				this.endpoints = new ArrayList<>();
			}
			this.endpoints.addAll(this.readEndpoints(data.valueOf("endpoints")));
		}
		if (data.variableExists("credential")) {
			Collection<UserNameAndPassword> credentials = this.readCredentials(data.valueOf("credential"));
			if (credentials.size() > 0) {
				this.credential = credentials.iterator().next();
			}
		}
	}

	public Collection<HostAndPort> getEndpoints() {
		return endpoints;
	}

	public void addEndpoints(Collection<HostAndPort> endpoints) {
		if (this.endpoints == null) {
			this.endpoints = new ArrayList<>();
		}
		this.endpoints.addAll(endpoints);
	}

	public void addEndpoint(HostAndPort endpoint) {
		if (this.endpoints == null) {
			this.endpoints = new ArrayList<>();
		}
		this.endpoints.add(endpoint);
	}

	public void addEndpoints(HostAndPort... endpoints) {
		this.addEndpoints(Arrays.asList(endpoints));
	}

	public UserNameAndPassword getCredential() {
		return credential;
	}

	public void setCredential(UserNameAndPassword credential) {
		this.credential = credential;
	}

	public void setCredential(String userName, String password) {
		this.credential = new UserNameAndPassword(userName, password);
	}

}
