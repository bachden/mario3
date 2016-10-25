package nhb.mario3.entity.message.transcoder.binary;

import nhb.common.data.PuElement;
import nhb.common.data.PuObject;
import nhb.common.exception.UnsupportedTypeException;
import nhb.mario3.entity.message.transcoder.MessageEncoder;

public class BinaryMessageSerializer implements MessageEncoder {

	@Override
	public Object encode(Object data) throws Exception {
		if (data == null) {
			return null;
		}
		if (data instanceof byte[]) {
			return (byte[]) data;
		} else if (data instanceof PuElement) {
			return ((PuObject) data).toBytes();
		} else if (data instanceof String) {
			return ((String) data).getBytes();
		}
		throw new UnsupportedTypeException();
	}
}
