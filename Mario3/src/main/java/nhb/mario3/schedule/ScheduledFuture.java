package nhb.mario3.schedule;

public interface ScheduledFuture {

	public long getId();

	public void cancel();

	public long getRemainingTimeToNextOccurrence();

	public long getStartTime();
}
