package nhb.mario3.exceptions;

public class InvalidDataFormatException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidDataFormatException() {

	}

	public InvalidDataFormatException(String message) {
		super(message);
	}

	public InvalidDataFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDataFormatException(Throwable cause) {
		super(cause);
	}
}
