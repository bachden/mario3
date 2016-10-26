package com.mario.entity.message;

import com.mario.entity.message.transcoder.MessageDecodingException;

public interface DecodingErrorMessage {

	MessageDecodingException getDecodingFailedCause();

	void setDecodingFailedCause(MessageDecodingException ex);
}
