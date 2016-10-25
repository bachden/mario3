package nhb.mario3.entity.message.transcoder;

import nhb.mario3.entity.message.MessageRW;

public interface MessageDecoder {

	void decode(Object data, MessageRW message) throws MessageDecodingException;
}
