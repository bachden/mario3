package com.mario.config.gateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.mario.exceptions.InvalidConfigException;
import com.mario.gateway.zeromq.ZeroMQGatewayType;
import com.nhb.common.utils.Converter;
import com.nhb.common.vo.ByteArray;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PRIVATE)
public class ZeroMQGatewayConfig extends GatewayConfig {

	private static final Set<String> SUB_KEY_TYPES = new HashSet<>(Arrays.asList("text", "base64", "hex"));

	private String endpoint;
	private String registryName;
	private ZeroMQGatewayType zeroMQGatewayType;

	private String threadNamePattern = "gateway-worker-#%d";
	private int queueSize = 1024;
	private int numSenders = 1;
	private int bufferCapacity = 1024 * 1024;
	private int messageBufferSize = 1024;
	private long hwm = (long) 1e6;
	private int numHandlers = 1;

	private final Set<ByteArray> subKeys = new HashSet<>();

	public ZeroMQGatewayConfig() {
		this.setType(GatewayType.ZEROMQ);
	}

	public Collection<byte[]> getListSubKeys() {
		List<byte[]> list = new ArrayList<>();
		for (ByteArray baw : this.subKeys) {
			list.add(baw.getSource());
		}
		return list;
	}

	private void readSubscribeKeys(Node item) {
		Node curr = item.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				Node typeNode = curr.getAttributes().getNamedItem("type");
				String type;
				if (typeNode == null) {
					type = "text";
				} else {
					type = typeNode.getNodeValue();
				}
				if (!SUB_KEY_TYPES.contains(type)) {
					throw new InvalidConfigException("ZeroMQ Gateway subKeys config only allow key type in list: "
							+ SUB_KEY_TYPES + ", got: " + type);
				} else {
					String value = curr.getTextContent().trim();
					byte[] valueBytes = null;
					switch (type) {
					case "text":
						valueBytes = value.getBytes();
						break;
					case "base64":
						valueBytes = Base64.getDecoder().decode(value);
						break;
					case "hex":
						valueBytes = Converter.hexToBytes(value);
						break;
					}
					this.subKeys.add(ByteArray.newInstanceWithJavaSafeHashCodeCalculator(valueBytes));
				}
			}
			curr = curr.getNextSibling();
		}
	}

	@Override
	public void readNode(Node item) {
		Node curr = item.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String value = curr.getTextContent().trim();
				switch (curr.getNodeName().trim().toLowerCase()) {
				case "name":
					this.setName(value);
					break;
				case "registry":
				case "registryname":
					this.setRegistryName(value);
					break;
				case "type":
					ZeroMQGatewayType type = ZeroMQGatewayType.forName(value);
					if (type == null) {
						throw new InvalidConfigException("ZeroMQ gateway type is invalid: " + value);
					}
					this.setZeroMQGatewayType(type);
					break;
				case "endpoint":
					this.setEndpoint(value);
					break;
				case "queuesize":
					this.setQueueSize(Integer.valueOf(value));
					break;
				case "numhandlers":
					this.setNumHandlers(Integer.valueOf(value));
					break;
				case "threadnamepattern":
					this.setThreadNamePattern(value);
					break;
				case "numsenders":
					this.setNumSenders(Integer.valueOf(value));
					break;
				case "messagebuffersize":
					this.setMessageBufferSize(Integer.valueOf(value));
					break;
				case "buffercapacity":
					this.setBufferCapacity(Integer.valueOf(value));
					break;
				case "hwm":
					this.setHwm(Long.valueOf(value));
					break;
				case "subkeys":
					this.readSubscribeKeys(curr);
					break;
				}
			}
			curr = curr.getNextSibling();
		}
	}
}
