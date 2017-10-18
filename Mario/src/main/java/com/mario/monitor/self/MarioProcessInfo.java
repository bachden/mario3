package com.mario.monitor.self;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

public class MarioProcessInfo implements MarioProcessInfoMXBean {

	private static final long SAMPLE_TIME = 500;

	@Getter
	private List<ThreadDetails> threadDetails = new ArrayList<>();

	private long nrCPUs = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	private ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
	private RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

	private Thread cpuUsageCalculatingWorker;

	public MarioProcessInfo() {

		this.cpuUsageCalculatingWorker = new Thread("Thread details updater") {
			public void run() {
				while (true) {
					updateThreadDetails();
				}
			}
		};

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cpuUsageCalculatingWorker.interrupt();
			}
		});

		this.cpuUsageCalculatingWorker.start();
	}

	private void updateThreadDetails() {
		ThreadInfo[] threadInfos = this.threadMxBean.dumpAllThreads(false, false);

		Map<Long, Long> threadInitialCpu = new HashMap<>();
		for (ThreadInfo info : threadInfos) {
			threadInitialCpu.put(info.getThreadId(), threadMxBean.getThreadCpuTime(info.getThreadId()));
		}

		long initialUptime = this.runtimeMxBean.getUptime();

		try {
			Thread.sleep(SAMPLE_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		List<ThreadDetails> threadDetails = new ArrayList<>();

		long elapsedTime = (runtimeMxBean.getUptime() - initialUptime);
		for (ThreadInfo info : threadInfos) {

			Long initialCPU = threadInitialCpu.get(info.getThreadId());
			if (initialCPU != null) {
				long currentCPU = threadMxBean.getThreadCpuTime(info.getThreadId());
				long elapsedCpu = currentCPU - initialCPU;
				float cpuUsage = elapsedCpu / (elapsedTime * 1e6f * nrCPUs);

				StringBuilder stackTraceBuidler = new StringBuilder();
				for (StackTraceElement ste : info.getStackTrace()) {
					stackTraceBuidler.append(stackTraceBuidler.length() > 0 ? "\n\tat " : "").append(ste.toString());
				}

				ThreadDetails details = ThreadDetails.builder() //
						.id(info.getThreadId()) //
						.name(info.getThreadName()) //
						.cpuUsage(cpuUsage) //
						.state(info.getThreadState()) //
						.stacktrace(stackTraceBuidler.toString()) //
						.build();
				threadDetails.add(details);
			}
		}

		try {
			Collections.sort(threadDetails);
			this.threadDetails = threadDetails;
		} catch (Exception ex) {
			// do nothing...
		}
	}

	@Override
	public int getThreadCount() {
		return this.threadMxBean.getThreadCount();
	}

	@Override
	public long getUptime() {
		return this.runtimeMxBean.getUptime();
	}
}
