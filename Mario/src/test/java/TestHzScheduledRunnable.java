import java.io.Serializable;

public class TestHzScheduledRunnable implements Runnable, Serializable {

	private static final long serialVersionUID = -8062024052215892338L;
	private final int id;

	public TestHzScheduledRunnable(int id) {
		this.id = id;
	}

	@Override
	public void run() {
		System.out.println("id: " + this.id);
	}

}
