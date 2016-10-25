package nhb.mario3.config;

import java.util.Collection;
import java.util.HashSet;

import nhb.common.data.PuObjectRO;
import nhb.common.db.cassandra.CassandraDatasourceConfig;
import nhb.common.vo.HostAndPort;

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
}
