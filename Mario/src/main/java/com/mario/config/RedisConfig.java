package com.mario.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.UnexpectedTypeException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.mario.extension.xml.EndpointReader;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.utils.ArrayUtils;
import com.nhb.common.utils.ArrayUtils.ForeachCallback;
import com.nhb.common.vo.HostAndPort;

import lombok.Getter;
import lombok.Setter;

public class RedisConfig extends MarioBaseConfig {

	public static enum RedisType {
		SINGLE, MASTER_SLAVE, SENTINEL, CLUSTER;

		public static RedisType fromName(String name) {
			if (name != null) {
				for (RedisType type : values()) {
					if (type.name().equalsIgnoreCase(name.trim())) {
						return type;
					}
				}
			}
			return null;
		}
	}

	@Getter
	private final List<HostAndPort> endpoints = new ArrayList<>();

	@Setter
	@Getter
	private RedisType redisType = RedisType.SINGLE;

	@Setter
	@Getter
	private String password;
	@Setter
	@Getter
	private int poolSize = 4;
	@Setter
	@Getter
	private int timeout = 10000;
	@Setter
	@Getter
	private int scanInterval = 2000;
	@Setter
	@Getter
	private String loadBalancer = null;

	@Setter
	@Getter
	private String masterName;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		if (data.variableExists("endpoints")) {
			this.getEndpoints().addAll(readEndpoints(data.valueOf("endpoints")));
		}

		if (data.variableExists("password")) {
			this.setPassword(data.getString("password"));
		}

		if (data.variableExists("poolSize")) {
			this.setPoolSize(data.getInteger("poolSize"));
		}

		if (data.variableExists("timeout")) {
			this.setTimeout(data.getInteger("timeout"));
		}

		if (data.variableExists("scanInterval")) {
			this.setScanInterval(data.getInteger("scanInterval"));
		}

		if (data.variableExists("loadBalancer")) {
			this.setLoadBalancer(data.getString("loadBalancer"));
		}

		if (data.variableExists("masterName")) {
			this.setMasterName(data.getString("masterName"));
		}

		if (data.variableExists("redisType")) {
			this.setRedisTypeByName(data.getString("redisType"));
		} else if (data.variableExists("type")) {
			this.setRedisTypeByName(data.getString("type"));
		}
	}

	public void addEndpoint(HostAndPort hostAndPort) {
		this.endpoints.add(hostAndPort);
	}

	public void addEndpoint(String host, int port) {
		this.addEndpoint(new HostAndPort(host, port));
	}

	public HostAndPort getFirstEndpoint() {
		return this.endpoints != null ? this.endpoints.get(0) : null;
	}

	public void setRedisTypeByName(String typeName) {
		this.setRedisType(RedisType.fromName(typeName));
	}

	@Override
	public void readNode(Node item) {
		Node curr = item.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String value = curr.getTextContent();
				switch (curr.getNodeName().trim().toLowerCase()) {
				case "name":
					this.setName(value);
					break;
				case "type":
					this.setRedisTypeByName(value);
					break;
				case "mastername":
					this.setMasterName(value);
					break;
				case "timeout":
					this.setTimeout(Integer.valueOf(value));
					break;
				case "poolsize":
					this.setPoolSize(Integer.valueOf(value));
					break;
				case "endpoint":
				case "endpoints":
					Object endpoint = EndpointReader.read(curr);
					if (endpoint instanceof Collection) {
						ArrayUtils.foreach(endpoint, new ForeachCallback<HostAndPort>() {

							@Override
							public void apply(HostAndPort element) {
								addEndpoint(element);
							}
						});
					} else if (endpoint instanceof HostAndPort) {
						addEndpoint((HostAndPort) endpoint);
					} else {
						throw new UnexpectedTypeException("");
					}
					break;
				}
			}
			curr = curr.getNextSibling();
		}
	}

}
