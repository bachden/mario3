package com.mario.contact;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nhb.common.Loggable;

public class ContactBook implements Loggable {

	private final Map<String, Contact> contacts = new ConcurrentHashMap<>();
	private final Map<String, ContactGroup> groups = new ConcurrentHashMap<>();

	private Contact readContact(Node node) {
		if (node == null) {
			throw new NullPointerException("Node config cannot be null");
		}
		Contact contact = new Contact();
		Node ele = node.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == Element.ELEMENT_NODE) {
				String eleName = ele.getNodeName();
				String eleValue = ele.getTextContent().trim();
				switch (eleName.toLowerCase()) {
				case "name":
					contact.setName(eleValue);
					break;
				case "email":
					contact.setEmail(eleValue);
					break;
				case "phone":
					contact.setPhoneNumber(eleValue);
					break;
				}
			}
			ele = ele.getNextSibling();
		}
		return contact;
	}

	private ContactGroup readGroup(Node node) {
		if (node == null) {
			throw new NullPointerException("Node config cannot be null");
		}
		ContactGroup group = new ContactGroup();
		Node ele = node.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == Element.ELEMENT_NODE) {
				String eleName = ele.getNodeName();
				switch (eleName.toLowerCase()) {
				case "name":
					String eleValue = ele.getTextContent().trim();
					group.setName(eleValue);
					break;
				case "members":
					Node entry = ele.getFirstChild();
					while (entry != null) {
						if (entry.getNodeType() == Element.ELEMENT_NODE
								&& entry.getNodeName().equalsIgnoreCase("contact")) {
							group.getContactNames().add(entry.getTextContent().trim());
						}
						entry = entry.getNextSibling();
					}
					break;
				}
			}
			ele = ele.getNextSibling();
		}
		return group;
	}

	public void readFromXml(Node node) {
		Node curr = node.getFirstChild();
		while (curr != null) {
			if (curr.getNodeType() == Element.ELEMENT_NODE) {
				String nodeName = curr.getNodeName();
				switch (nodeName.toLowerCase()) {
				case "contact":
					this.addContact(readContact(curr));
					break;
				case "group":
					this.addGroup(readGroup(curr));
					break;
				}
			}
			curr = curr.getNextSibling();
		}
	}

	public void addGroup(ContactGroup group) {
		if (group == null) {
			throw new NullPointerException("Group cannot be null");
		}
		this.groups.put(group.getName(), group);
	}

	public void addContact(Contact contact) {
		if (contact == null) {
			throw new NullPointerException("Adding concact cannot be null");
		}
		this.contacts.put(contact.getName(), contact);
	}

	public Contact getContact(String contactName) {
		return this.contacts.get(contactName);
	}

	public ContactGroup getGroup(String groupName) {
		if (groupName == null) {
			throw new NullPointerException("Group name must be specficed");
		}
		if (!this.groups.containsKey(groupName)) {
			throw new NullPointerException("ContactGroup cannot be found for name: " + groupName);
		}
		return this.groups.get(groupName);
	}

	public Collection<Contact> getContactByGroup(String groupName) {
		ContactGroup group = this.getGroup(groupName);
		Collection<Contact> results = new HashSet<>();
		for (String contactName : group.getContactNames()) {
			Contact contact = this.getContact(contactName);
			if (contact != null) {
				results.add(contact);
			} else {
				getLogger().error("Contact name {} in group {} cannot be found", contactName, groupName,
						new NullPointerException());
			}
		}
		return results;
	}
}
