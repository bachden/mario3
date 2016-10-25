package nhb.mario3.monitor;

import nhb.mario3.entity.LifeCycle;
import nhb.mario3.entity.Pluggable;

public interface MonitorAgent extends LifeCycle, Pluggable {

	public void start();

	public void stop();

	public void setInterval(int interval);

	public void monitor(Monitorable monitorable);
}
