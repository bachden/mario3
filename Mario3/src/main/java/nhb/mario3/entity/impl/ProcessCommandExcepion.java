package nhb.mario3.entity.impl;

public class ProcessCommandExcepion extends RuntimeException {

	private static final long serialVersionUID = -6118388254514179081L;

	public ProcessCommandExcepion() {
		// do nothing
	}

	public ProcessCommandExcepion(Throwable cause) {
		super(cause);
	}

	public ProcessCommandExcepion(String message, Throwable cause) {
		super(message, cause);
	}
}
