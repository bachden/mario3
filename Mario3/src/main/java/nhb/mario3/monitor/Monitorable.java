package nhb.mario3.monitor;

public interface Monitorable {

	String getId();

	MonitorableStatus checkStatus();

	void resume();
}
