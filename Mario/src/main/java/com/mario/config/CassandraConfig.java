package com.mario.config;

import java.util.Collection;
import java.util.HashSet;

import org.w3c.dom.Node;

import com.mario.extension.xml.EndpointReader;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.db.cassandra.CassandraDatasourceConfig;
import com.nhb.common.vo.HostAndPort;

public class CassandraConfig extends MarioBaseConfig implements CassandraDatasourceConfig {

	private Collection<HostAndPort> endpoints = new HashSet<>();
	private String keyspace;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("keyspace")) {
			this.setKeyspace(data.getString("keyspace"));
		}
		if (data.variableExists("endpoints")) {
			this.setEndpoints(this.readEndpoints(data.valueOf("endpoints")));
		}
	}

	@Override
	public Collection<HostAndPort> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(Collection<HostAndPort> endpoints) {
		this.endpoints = endpoints;
	}

	@Override
	public String getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readNode(Node item) {
		Node currNode = item.getFirstChild();
		while (currNode != null) {
			if (currNode.getNodeType() == 1) {
				if (currNode.getNodeName().equalsIgnoreCase("name")) {
					this.setName(currNode.getTextContent().trim());
				} else if (currNode.getNodeName().equalsIgnoreCase("endpoint")) {
					Object obj = EndpointReader.read(currNode);
					if (obj instanceof HostAndPort) {
						this.getEndpoints().add((HostAndPort) obj);
					} else if (obj instanceof Collection<?>) {
						this.getEndpoints().addAll((Collection<? extends HostAndPort>) obj);
					}
				} else if (currNode.getNodeName().equalsIgnoreCase("keyspace")) {
					this.setKeyspace(currNode.getTextContent().trim());
				}
			}
			currNode = currNode.getNextSibling();
		}
	}
}
