package nhb.mario3.entity.message.transcoder.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.msgpack.MessagePack;

import nhb.common.BaseLoggable;
import nhb.common.data.PuElement;
import nhb.common.data.msgpkg.PuElementTemplate;
import nhb.mario3.entity.message.MessageRW;
import nhb.mario3.entity.message.transcoder.MessageDecoder;

public class BinaryMessageDeserializer extends BaseLoggable implements MessageDecoder {

	private static final MessagePack msgpack = new MessagePack();

	@Override
	public void decode(Object data, MessageRW message) {
		if (data != null) {
			PuElement puo;
			try {
				puo = PuElementTemplate.getInstance()
						.read(msgpack.createUnpacker(new ByteArrayInputStream((byte[]) data)), null);
				message.setData(puo);
			} catch (IOException e) {
				throw new RuntimeException("Unable to decode binary: " + new String((byte[]) data), e);
			}
		}
	}
}
