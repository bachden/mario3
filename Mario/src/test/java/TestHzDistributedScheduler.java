import java.util.concurrent.TimeUnit;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.mario.schedule.distributed.exception.DistributedScheduleException;
import com.mario.schedule.distributed.impl.HzDistributedScheduler;

public class TestHzDistributedScheduler {

	public static void main(String[] args) throws DistributedScheduleException {
		HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance();
		HzDistributedScheduler scheduler = new HzDistributedScheduler("test_scheduler", hazelcast);

		for (int i = 0; i < 1e6; i++) {
			scheduler.schedule("task_" + i, new TestHzScheduledRunnable(i), 100, TimeUnit.MILLISECONDS);
		}
	}

}
