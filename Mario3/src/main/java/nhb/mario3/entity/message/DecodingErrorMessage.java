package nhb.mario3.entity.message;

import nhb.mario3.entity.message.transcoder.MessageDecodingException;

public interface DecodingErrorMessage {

	MessageDecodingException getDecodingFailedCause();

	void setDecodingFailedCause(MessageDecodingException ex);
}
