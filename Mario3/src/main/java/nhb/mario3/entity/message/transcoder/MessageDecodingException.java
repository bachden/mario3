package nhb.mario3.entity.message.transcoder;

import nhb.mario3.entity.message.Message;

public class MessageDecodingException extends Exception {

	private static final long serialVersionUID = 1L;

	private final Message target;

	public MessageDecodingException(Message target, Throwable cause) {
		super(cause);
		this.target = target;
	}

	public Message getTarget() {
		return target;
	}
}
