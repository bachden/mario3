package com.mario.schedule.distributed.exception;

public class DistributedScheduleException extends Exception {

	private static final long serialVersionUID = 1L;

	public DistributedScheduleException() {
		super();
	}

	public DistributedScheduleException(String message) {
		super(message);
	}

	public DistributedScheduleException(String message, Throwable cause) {
		super(message, cause);
	}

	public DistributedScheduleException(Throwable cause) {
		super(cause);
	}
}
