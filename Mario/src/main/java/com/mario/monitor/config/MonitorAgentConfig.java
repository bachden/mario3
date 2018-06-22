package com.mario.monitor.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.mario.config.MarioBaseConfig;
import com.mario.exceptions.OperationNotSupported;
import com.mario.monitor.MonitorableStatus;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MonitorAgentConfig extends MarioBaseConfig {

	private long interval;
	private String target;
	private PuObjectRO monitoringParams;
	private MonitorAlertConfig alertConfig;
	private String schedulerName;

	@Override
	protected void _readPuObject(PuObjectRO data) {
		throw new OperationNotSupported();
	}

	@Override
	public void readNode(Node item) {
		Node ele = item.getFirstChild();
		while (ele != null) {
			if (ele.getNodeType() == Element.ELEMENT_NODE) {
				String eleName = ele.getNodeName();
				switch (eleName) {
				case "name":
					this.setName(ele.getTextContent().trim());
					break;
				case "scheduler":
					this.setSchedulerName(ele.getTextContent().trim());
					break;
				case "target":
					this.setTarget(ele.getTextContent().trim());
					break;
				case "interval":
					this.setInterval(Long.valueOf(ele.getTextContent().trim()));
					break;
				case "alert":
					this.setAlertConfig(this.readMonitorAlertConfig(ele));
					break;
				case "variables":
					this.setMonitoringParams(PuObject.fromXML(ele));
					break;
				}
			}
			ele = ele.getNextSibling();
		}
	}

	private MonitorAlertConfig readMonitorAlertConfig(Node node) {
		if (node != null) {
			Node curr = node.getFirstChild();
			MonitorAlertConfig config = new MonitorAlertConfig();
			while (curr != null) {
				if (curr.getNodeType() == Element.ELEMENT_NODE) {
					String nodeName = curr.getNodeName().toLowerCase();
					if (nodeName.equalsIgnoreCase("autoSendRecovery")) {
						Boolean value = Boolean.valueOf(curr.getTextContent().trim());
						config.setAutoSendRecovery(value);
					} else {
						MonitorableStatus status = MonitorableStatus.fromName(nodeName);
						if (status != null) {
							MonitorAlertStatusConfig statusConfig = new MonitorAlertStatusConfig();
							statusConfig.setStatus(status);

							Node statusEle = curr.getFirstChild();
							while (statusEle != null) {
								if (statusEle.getNodeType() == Element.ELEMENT_NODE) {
									String statusEleName = statusEle.getNodeName().toLowerCase();
									switch (statusEleName) {
									case "recipients":
										MonitorAlertRecipientsConfig recipientsConfig = new MonitorAlertRecipientsConfig();
										Node recipientsEle = statusEle.getFirstChild();
										while (recipientsEle != null) {
											if (recipientsEle.getNodeType() == Element.ELEMENT_NODE) {
												String recipientsNodeName = recipientsEle.getNodeName().toLowerCase();
												switch (recipientsNodeName) {
												case "contact":
													recipientsConfig.getContacts()
															.add(recipientsEle.getTextContent().trim());
													break;
												case "group":
													recipientsConfig.getGroups()
															.add(recipientsEle.getTextContent().trim());
													break;
												}
											}
											recipientsEle = recipientsEle.getNextSibling();
										}
										statusConfig.setRecipientsConfig(recipientsConfig);
										break;
									case "services":
										MonitorAlertServicesConfig servicesConfig = new MonitorAlertServicesConfig();
										Node servicesEle = statusEle.getFirstChild();
										while (servicesEle != null) {
											if (servicesEle.getNodeType() == Element.ELEMENT_NODE) {
												String servicesNodeName = servicesEle.getNodeName().toLowerCase();
												switch (servicesNodeName) {
												case "sms":
													servicesConfig.getSmsServices()
															.add(servicesEle.getTextContent().trim());
													break;
												case "email":
													servicesConfig.getEmailServices()
															.add(servicesEle.getTextContent().trim());
													break;
												case "telegram":
												case "telegrambot":
													servicesConfig.getTelegramBots()
															.add(servicesEle.getTextContent().trim());
													break;
												}
											}
											servicesEle = servicesEle.getNextSibling();
										}
										statusConfig.setServicesConfig(servicesConfig);
										break;
									}
								}
								statusEle = statusEle.getNextSibling();
							}
							config.getStatusToConfigs().put(status, statusConfig);
						}
					}
				}
				curr = curr.getNextSibling();
			}
			return config;
		}
		return null;
	}
}
