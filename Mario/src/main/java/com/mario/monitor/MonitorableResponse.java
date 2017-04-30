package com.mario.monitor;

import com.nhb.common.data.PuElement;

public interface MonitorableResponse {

	MonitorableStatus getStatus();

	String getMessage();

	PuElement getDetails();

}
