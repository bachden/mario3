package com.mario.schedule.distributed.impl;

import java.io.Serializable;

import com.hazelcast.core.HazelcastInstanceAware;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public class HzDistributedRunnableWrapper extends HzDistributedScheduleCleaner implements Runnable, Serializable {

	private static final long serialVersionUID = 7251235487019550384L;

	private final Runnable runner;

	@Getter
	private final boolean autoClean;

	public HzDistributedRunnableWrapper(String schedulerName, boolean autoClean, String taskName,
			String trackingMapName, Runnable runner) {
		super(schedulerName, taskName, trackingMapName);
		if (!(runner instanceof Serializable)) {
			throw new IllegalArgumentException("Runner must be instanceof Serializable");
		}
		this.runner = runner;
		this.autoClean = autoClean;
	}

	@Override
	public void run() {
		if (this.runner instanceof HazelcastInstanceAware) {
			((HazelcastInstanceAware) this.runner).setHazelcastInstance(this.getHazelcast());
		}

		try {
			this.runner.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (this.autoClean) {
				this.cleanTask();
			}
		}
	}
	//
	// public static class DummyRunnable implements DistributedRunnable {
	//
	// private static final long serialVersionUID = 3279013156170740326L;
	//
	// @Override
	// public void run() {
	// System.out.println("test");
	// }
	//
	// }
	//
	// public static void main(String[] args) throws IOException,
	// ClassNotFoundException {
	// HzDistributedRunnableWrapper child = new HzDistributedRunnableWrapper("abc",
	// true, "task", "trackingMap",
	// new DummyRunnable());
	// ByteArrayOutputStream fos = new ByteArrayOutputStream();
	// ObjectOutputStream oos = new ObjectOutputStream(fos);
	// oos.writeObject(child);
	// byte[] bytes = fos.toByteArray();
	// oos.close();
	//
	// ByteArrayInputStream fis = new ByteArrayInputStream(bytes);
	// ObjectInputStream ois = new ObjectInputStream(fis);
	// Object obj = ois.readObject();
	// ois.close();
	//
	// HzDistributedRunnableWrapper deserializedObject =
	// (HzDistributedRunnableWrapper) obj;
	//
	// System.out.println(deserializedObject);
	// }
}
