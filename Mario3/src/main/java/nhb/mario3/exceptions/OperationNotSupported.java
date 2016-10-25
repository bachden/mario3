package nhb.mario3.exceptions;

public class OperationNotSupported extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OperationNotSupported() {

	}

	public OperationNotSupported(String message) {
		super(message);
	}

	public OperationNotSupported(String message, Throwable cause) {
		super(message, cause);
	}

	public OperationNotSupported(Throwable cause) {
		super(cause);
	}
}
