package com.mario.config.serverwrapper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

public class ZeroMQServerWrapperConfig extends ServerWrapperConfig {

	@Setter
	@Getter
	private String zeroMQRegistryName;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		this.setName(data.getString("name", null));
		this.setZeroMQRegistryName(data.getString("zeroMQRegistryName", data.getString("zeromqregistryname", null)));
		if (this.getZeroMQRegistryName() == null) {
			throw new NullPointerException("zeroMQ registry name cannot be null");
		}
	}

	public void readNode(Node item) {
		if (item == null) {
			throw new NullPointerException("Node item cannot be null");
		}
		Node curr = item.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				switch (curr.getNodeName().trim().toLowerCase()) {
				case "name":
					this.setName(curr.getTextContent().trim());
					break;
				case "registry":
				case "registryname":
					this.setZeroMQRegistryName(curr.getTextContent().trim());
					break;
				}
			}
			curr = curr.getNextSibling();
		}
	}

}
