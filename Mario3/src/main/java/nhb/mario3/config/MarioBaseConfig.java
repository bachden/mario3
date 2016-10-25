package nhb.mario3.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import nhb.common.BaseLoggable;
import nhb.common.data.PuArray;
import nhb.common.data.PuDataType;
import nhb.common.data.PuElement;
import nhb.common.data.PuObject;
import nhb.common.data.PuObjectRO;
import nhb.common.data.PuValue;
import nhb.common.data.exception.InvalidDataException;
import nhb.common.vo.HostAndPort;
import nhb.common.vo.UserNameAndPassword;

public abstract class MarioBaseConfig extends BaseLoggable {

	private String extensionName;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtensionName() {
		return extensionName;
	}

	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

	private final Function<String, Collection<HostAndPort>> hostAndPortStringConsumer = (str) -> {
		if (str != null) {
			String[] arr = str.trim().split(",");
			if (arr.length > 0) {
				for (String entry : arr) {
					String[] eles = entry.trim().split(":");
					if (eles.length > 0) {
						HostAndPort hostAndPort = new HostAndPort();
						String host = eles[0].trim();
						if (host.length() > 0) {
							hostAndPort.setHost(host);
						} else {
							throw new InvalidDataException("host and port error: " + str);
						}
						if (eles.length > 1) {
							int port = Integer.parseInt(eles[1].trim());
							if (port > 0) {
								hostAndPort.setPort(port);
							}
						}
					}
				}
			}
		}
		return null;
	};

	private Function<PuObject, HostAndPort> hostAndPortPuObjectConsumer = (puo) -> {
		if (puo != null) {
			HostAndPort result = new HostAndPort();
			if (puo.variableExists("host")) {
				result.setHost(puo.getString("host"));
			}
			if (puo.variableExists("port")) {
				result.setPort(puo.getInteger("port"));
			}
			if (puo.variableExists("master")) {
				result.setMaster(puo.getBoolean("master"));
			} else if (puo.variableExists("isMaster")) {
				result.setMaster(puo.getBoolean("isMaster"));
			}
			if (puo.variableExists("useSsl")) {
				result.setUseSSL(puo.getBoolean("useSsl"));
			} else if (puo.variableExists("useSSL")) {
				result.setUseSSL(puo.getBoolean("useSSL"));
			}
			return result;
		}
		return null;
	};

	private Function<PuArray, Collection<HostAndPort>> puArrayConsumer = new Function<PuArray, Collection<HostAndPort>>() {

		@Override
		public Collection<HostAndPort> apply(PuArray puarray) {
			if (puarray != null) {
				Collection<HostAndPort> endpoints = new ArrayList<>();
				for (PuValue value : puarray) {
					endpoints.addAll(puValueConsumer.apply(value));
				}
				return endpoints;
			}
			return null;
		}
	};

	private Function<PuValue, Collection<HostAndPort>> puValueConsumer = new Function<PuValue, Collection<HostAndPort>>() {

		@Override
		public Collection<HostAndPort> apply(PuValue puValue) {
			if (puValue != null) {
				Collection<HostAndPort> endpoints = new ArrayList<>();
				switch (puValue.getType()) {
				case PUOBJECT:
					endpoints.add(hostAndPortPuObjectConsumer.apply(puValue.getPuObject()));
					break;
				case PUARRAY:
					endpoints.addAll(puArrayConsumer.apply(puValue.getPuArray()));
					break;
				case RAW:
					puValue.setType(PuDataType.STRING);
				case STRING:
					endpoints.addAll(hostAndPortStringConsumer.apply(puValue.getString()));
					break;
				default:
					throw new InvalidDataException("Invalid host and port config: " + puValue.toString());
				}
				return endpoints;
			}
			return null;
		}
	};

	protected Collection<HostAndPort> readEndpoints(PuElement info) {
		if (info == null) {
			return null;
		}

		Collection<HostAndPort> endpoints = new ArrayList<>();
		if (info instanceof PuValue) {
			endpoints = puValueConsumer.apply((PuValue) info);
		} else if (info instanceof PuObject) {
			endpoints.add(hostAndPortPuObjectConsumer.apply((PuObject) info));
		} else if (info instanceof PuArray) {
			endpoints = puArrayConsumer.apply((PuArray) info);
		}
		return endpoints;
	}

	private Function<PuObject, UserNameAndPassword> credentialPuObjectConsumer = new Function<PuObject, UserNameAndPassword>() {

		@Override
		public UserNameAndPassword apply(PuObject t) {
			if (t != null) {
				UserNameAndPassword result = new UserNameAndPassword();
				if (t.variableExists("userName")) {
					result.setUserName(t.getString("userName"));
				} else if (t.variableExists("username")) {
					result.setUserName(t.getString("username"));
				}
				if (t.variableExists("password")) {
					result.setPassword(t.getString("password"));
				} else if (t.variableExists("pwd")) {
					result.setPassword(t.getString("pwd"));
				}
				return result;
			}
			return null;
		}
	};

	private Function<PuArray, Collection<UserNameAndPassword>> credentialPuArrayConsumer = new Function<PuArray, Collection<UserNameAndPassword>>() {

		@Override
		public Collection<UserNameAndPassword> apply(PuArray t) {
			if (t != null) {
				Collection<UserNameAndPassword> certificates = new ArrayList<>();
				for (PuValue value : t) {
					certificates.addAll(credentialPuValueConsumer.apply(value));
				}
			}
			return null;
		}
	};

	private Function<PuValue, Collection<UserNameAndPassword>> credentialPuValueConsumer = new Function<PuValue, Collection<UserNameAndPassword>>() {

		@Override
		public Collection<UserNameAndPassword> apply(PuValue t) {
			if (t != null) {
				Collection<UserNameAndPassword> certificates = new ArrayList<>();
				switch (t.getType()) {
				case PUOBJECT:
					certificates.add(credentialPuObjectConsumer.apply(t.getPuObject()));
					break;
				case PUARRAY:
					certificates.addAll(credentialPuArrayConsumer.apply(t.getPuArray()));
					break;
				default:
					throw new InvalidDataException("certificate config is informal");
				}
			}
			return null;
		}
	};

	protected Collection<UserNameAndPassword> readCredentials(PuElement info) {
		if (info == null) {
			return null;
		}
		Collection<UserNameAndPassword> credentials = new ArrayList<>();
		if (info instanceof PuValue) {
			credentials.addAll(credentialPuValueConsumer.apply((PuValue) info));
		} else if (info instanceof PuObject) {
			credentials.add(credentialPuObjectConsumer.apply((PuObject) info));
		} else if (info instanceof PuArray) {
			credentials.addAll(credentialPuArrayConsumer.apply((PuArray) info));
		} else {
			throw new InvalidDataException("Credential info invalid" + info.toString());
		}
		return credentials;
	}

	public final void readPuObject(PuObjectRO data) {
		if (data.variableExists("name")) {
			this.name = data.getString("name");
		}
		this._readPuObject(data);
	}

	protected abstract void _readPuObject(PuObjectRO data);
}
