package com.mario.config.serverwrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.w3c.dom.Node;

import com.mario.extension.xml.CredentialReader;
import com.mario.extension.xml.EndpointReader;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.vo.HostAndPort;
import com.nhb.common.vo.UserNameAndPassword;

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

	@SuppressWarnings("unchecked")
	public void readNode(Node item) {
		Node curr = item.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == 1) {
				String nodeName = curr.getNodeName().trim().toLowerCase();
				switch (nodeName) {
				case "endpoint":
					System.out.println("\t\t\t\t- Reading endpoint info");
					Object endpoint = EndpointReader.read(curr);
					if (endpoint instanceof HostAndPort) {
						this.addEndpoint((HostAndPort) endpoint);
					} else if (endpoint instanceof Collection) {
						this.addEndpoints((Collection<HostAndPort>) endpoint);
					}
					break;
				case "credential":
					System.out.println("\t\t\t\t- Reading credential info");
					Object credential = CredentialReader.read(curr);
					if (credential instanceof UserNameAndPassword) {
						this.setCredential((UserNameAndPassword) credential);
					}
					break;
				case "name":
					System.out.println("\t\t\t\t- Reading name info");
					this.setName(curr.getTextContent().trim());
					break;
				case "autoreconnect":
					System.out.println("\t\t\t\t- Reading autoreconnect info");
					getLogger().warn("Autoreconnect is default and cannot be set, it's deprecated");
					break;
				default:
					System.out.println("\t\t\t\t- !!! ERROR !!! --> invalid tag name: " + curr.getNodeName());
					// throw new RuntimeException("invalid tag name:
					// " + curr.getNodeName());
				}
			}
			curr = curr.getNextSibling();
		}
	}

}
