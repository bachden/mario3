package com.mario.monitor.self;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class MarioSelfMonitor {

	public void start() throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		mbs.registerMBean(new MarioProcessInfo(),
				new ObjectName("com.mario:type=" + MarioProcessInfo.class.getSimpleName()));
	}
}
